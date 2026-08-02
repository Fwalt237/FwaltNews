# FwaltNews : An AI-powered news aggregation platform

FwaltNews is a production-grade, highly available full-stack news aggregation platform featuring edge-delivered frontend assets via Cloudflare Pages, a containerized serverless REST API running on AWS ECS Fargate, and an intelligent AI assistant powered by Google Gemini.

This project was architected to demonstrate modern cloud-native engineering practices, including Infrastructure as Code with Terraform, secure multi-tier VPC networking with an application load balancer for fault tolerance, vector-based semantic search using pgvector and Gemini embeddings, hybrid authentication (JWT + OAuth2), automated content ingestion pipelines, and fully automated CI/CD container lifecycle management through GitHub Actions.

Demo:

https://github.com/user-attachments/assets/669c5d09-7b11-4fb6-914b-227fe1a67d2e





## Why this project exists

I come from a non‑CS background (civil engineering), but I decided to switch to tech because, in the long term, I want to humbly contribute to the growth of the tech industry in my home country, Cameroon, which still has enormous room for improvement.

Once I made the decision, the next step was finding the right place to learn the fundamentals and modern tools to match that goal. I was extremely fortunate to discover **MJC School** through a Reddit post. It’s a diamond that too few people know about: a completely free platform with a dedicated team of engineer‑mentors and a tailored curriculum that can take you from zero to a junior software engineer.  

The project was built progressively, starting from a solid foundation. But I saw an opportunity to go further, to take that foundation and add layer upon layer to make it a true end‑to‑end, production‑grade system. It became the perfect sandbox to learn industry standards.

In parallel, I deepened my knowledge through:

- **The Odin Project** : sharpening my front‑end skills
- **University of Helsinki MOOC Center** : containerization with *DevOps with Docker* and the first three chapters of *DevOps with Kubernetes*
- **HashiCorp Developer Tutorials** : Infrastructure as Code for AWS
- **Dan Vega’s blog and YouTube channel** : Spring AI and generative AI integration

I named the project FwaltNews because *Fwalt* comes from my own name, and *News* reflects what it does, it’s a news aggregation platform.

In the rest of this documentation, I’ll walk you through the atomic composition of the project: every architectural decision, every line of infrastructure code, and every lesson learned along the way.





## Try it live

|                |                                                              |
| -------------- | ------------------------------------------------------------ |
| **Frontend**   | [fwaltnews.com](https://fwaltnews.com)                       |
| **REST API**   | [api.fwaltnews.com](https://api.fwaltnews.com)               |
| **Swagger UI** | [api.fwaltnews.com/swagger-ui/index.html](https://api.fwaltnews.com/swagger-ui/index.html) |
| **CI/CD**      | https://github.com/Fwalt237/FwaltNews/actions                |
| **SonarCloud** | https://sonarcloud.io/project/overview?id=Fwalt237_FwaltNews |

**Quick demo path:**

1. Go to [fwaltnews.com](https://fwaltnews.com) and sign up
2. Browse the latest news fetched automatically from NewsData.io
3. Click the 🧙‍♂️ chat button (bottom right) and ask: *"What's the hottest news this week?"*
4. Watch the AI search the actual article database and surface relevant stories



## Architecture

<img width="462" height="692" alt="Architecture" src="https://github.com/user-attachments/assets/1da42a01-57e2-4484-b39c-12e35a5a3858" />
<?xml version="1.0" encoding="UTF-8"?>



The project uses Cloudflare as the entry point for everything. The domain `fwaltnews.com` was registered through Cloudflare and its DNS is managed there. The React frontend is deployed to Cloudflare Pages, which automatically builds and hosts the static assets on a global CDN; both the root domain and `www` subdomain point to this Pages deployment, with automatic HTTPS and redirects configured. For the backend, a CNAME record `api.fwaltnews.com` resolves to an AWS Application Load Balancer that was created via Terraform and placed in two public subnets. 

The ALB listens on port 80 and forwards requests to a target group that checks the health of the Spring Boot application at `/actuator/health`. That target group routes traffic to ECS Fargate tasks running inside an ECS cluster and service spread across the same public subnets. Each Fargate task gets a public IP address (to avoid NAT Gateway costs) and is protected by a security group  that only allows incoming traffic on port 8080 from the ALB’s security group.

 The backend containers are built from a multi‑stage Dockerfile and stored in an ECR repository ; their environment variables and secrets are injected at runtime from AWS Parameter Store, including database credentials, JWT secret, and all third‑party API keys. The PostgreSQL database is accessible only from the ECS security group on port 5432. The database has the `pgvector` extension enabled via Flyway migration and stores news article embeddings for semantic search. 

The entire AWS infrastructure: VPC, subnets, route tables, Internet Gateway, security groups, RDS, ALB, ECS cluster/service/task definition, IAM roles, CloudWatch log group, and SSM parameters, is defined and provisioned with Terraform. A GitHub Actions workflow triggers on pushes to main (only when backend files change), runs tests, builds a Docker image, pushes it to ECR, and forces an ECS service redeployment, completing the fully automated CI/CD pipeline.



## Key technical features

### AI assistant (Spring AI + Gemini)

The assistant doesn't use general AI knowledge. It reasons over The actual database.

When a user asks a question, the backend generates a 768‑dimensional query vector using Google’s `gemini-embedding-001` model (via `spring.ai.google.genai.embedding.text.options.model`).

That vector is compared to pre‑computed article embeddings stored in PostgreSQL’s `pgvector` extension using a **cosine similarity** (`<=>`) operator. The similarity search is accelerated by an **HNSW index** built on the `news_embeddings` table:

CREATE INDEX IF NOT EXISTS idx_news_embeddings_hnsw
    ON news_embeddings USING hnsw (embedding vector_cosine_ops)
    WITH (m=16, ef_construction=64);

The **HNSW (Hierarchical Navigable Small World)** index is an approximate nearest neighbor search structure that balances query speed and accuracy. It builds a multi‑layer graph: the top layers act as shortcuts for fast navigation, while the bottom layer stores all vectors for precise local search.
During index creation, `m=16` sets the maximum number of connections each node can have, a higher value improves search recall but uses more memory and build time. `ef_construction=64` controls the size of the dynamic candidate list while building the graph; larger values produce a higher‑quality index at the expense of longer construction time. At query time, a separate `ef_search` parameter (not stored in the index) can be tuned to further balance speed against recall.

The top‑k most semantically relevant articles are retrieved, their content is fed as context to Gemini 3.1 Flash‑lite via Spring AI, and a grounded answer is returned along with the source article IDs. The frontend renders the answer and corresponding article cards.

Every article is embedded at ingestion time and stored natively in a vector(768) column, no external vector database is required. The entire retrieval pipeline is a single, efficient SQL query.

#### MCP Tools with Spring AI

The assistant is implemented with Spring AI’s `ChatClient` and **function‑calling**. Gemini doesn’t just answer with words, it can request structured data by calling predefined Java methods. This is exposed through `NewsTools`, a Spring‑managed bean with four `@Tool` methods:

- **`searchNewsByTopic(query, hoursBack, limit)`** : embeds the user’s query, runs the pgvector similarity search, and returns article IDs with titles and excerpts.
- **`getLatestNewsByTag(tag, hoursBack, limit)`** : fetches articles by category (tech, sports, etc.) filtered by recency.
- **`getTopRecentNews(hoursBack, limit)`** : retrieves the most recent articles for a “daily briefing” or “what happened today” style request.
- **`getFullArticle(newsId)`** : returns the complete text of a single article (used for summarisation or follow‑up “tell me more” prompts).

The `AiAssistantService` wires these tools into a `ChatClient` at startup:

this.chatClient = chatClientBuilder.defaultTools(newsTools).build();

When the user sends a message, the service appends it to a conversation history (stored in the `chat_messages` table), prepends a carefully engineered system prompt, and invokes the `ChatClient`. Gemini decides which tool(s) to call, the tools are executed on the server side, and the results are fed back into the model for the final answer.

#### Conversation memory

Chat history is persisted per `sessionId`. The `chat_messages` table stores each turn (`user`/`assistant`) with a timestamp. To keep context manageable, the service only sends the last **N turns** to Gemini (`app.ai.max-history-turns=5`). Users can also clear their history via a dedicated endpoint (`DELETE /api/v1/ai/history/{sessionId}`), which deletes all messages for that session.

#### Structured output parsing

The system prompt instructs Gemini to include a special marker line when it has new article references:

ARTICLES_FOUND:[1,2,3]

After receiving the AI reply, `AiAssistantService` strips that line from the text displayed to the user and parses it into a `List<Long>` of article IDs. This allows the frontend to show both the assistant’s message and the actual news cards referenced in the answer.

#### Embedding pipeline (ingestion side)

Every news article is embedded at ingestion time. The `EmbeddingService` provides an `@Async` method `embedNews(newsId)` that:

- Checks if an embedding already exists (idempotent).
- Fetches the article, builds a combined text from title + cleaned content.
- Calls the Gemini embedding API via `MyEmbeddingClient` (a custom `RestTemplate`‑based client that wraps the REST endpoint `https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent`).
- Persists the resulting 768‑dimension `float[]` vector as a native `vector(768)` PostgreSQL column.

The `@Async` annotation (backed by Spring’s `ThreadPoolTaskExecutor`) ensures that embedding does not block the main request thread. A scheduled job `embedMissing` runs daily and processes articles that lack embeddings – with a ShedLock ensuring only one Fargate task runs it at a time.

The `MyEmbeddingClient` performs validation: it checks the HTTP status, verifies the vector dimension (exactly 768), and throws an `AiIntegrationException` on failure, which the global exception handler translates into a 500 error with a clear code.

#### Why this architecture ?

- **No vendor lock‑in** : the embedding client is a simple REST adapter; switching to another provider (or a self‑hosted model) only requires a new implementation.
- **Real‑time data** : the assistant’s answers are always grounded in the latest articles, not a stale snapshot.
- **Observable** : every tool invocation and embedding operation is logged with detailed parameters, making debugging straightforward.
- **Scalable** : the pgvector index supports efficient similarity search even with thousands of articles; function‑calling keeps the interaction stateful without massive context windows.



### Authentication & Authorization (Spring Security)

The application uses a fully stateless authentication model: no server‑side sessions, no JSESSIONID cookies. Every request is authenticated via a JWT token passed in the `Authorization` header, and the security filter chain is carefully tuned for a single‑page application.

#### Security filter chain (`SecurityConfig.filterChain()`)

- **CORS** : custom `CorsConfigurationSource` allows a list of origin patterns (production domain, Cloudflare preview URLs, localhost). The configuration supports credentials (cookies/headers) and exposes the `Authorization` and `x-auth-token` headers to the frontend.
- **CSRF** : disabled. This is safe because the API is stateless (no cookies) and the frontend sends the JWT as a header.
- **Session management** : set to `SessionCreationPolicy.IF_REQUIRED`. Spring still creates a temporary session during the OAuth2 redirect flow, but it is never used for authentication afterwards.
- **Exception handling** : a custom `AuthenticationEntryPoint` returns a `401` JSON response (`{"message": "Unauthorized access - please login"}`) instead of a default login page, so the React app can handle it gracefully.
- **Authorization rules** : fine‑grained HTTP method + path matching:
  - Public endpoints: `/api/v*/auth/**`, `/oauth2/**`, Swagger UI, `/actuator/**`, and most `GET` endpoints (news, authors, tags, comments, AI) – `permitAll()`
  - `POST` endpoints for news, comments, AI – require `USER` or `ADMIN` role
  - All other `POST`, `PATCH`, `PUT`, `DELETE` operations – require `ADMIN`
  - Everything else : `authenticated()`

#### JWT Authentication (`JwtAuthenticationFilter`)

The filter extends `OncePerRequestFilter` and runs **before** `UsernamePasswordAuthenticationFilter`.

- **Whitelist** : paths like Swagger UI, Actuator, OAuth2 callbacks, and authentication endpoints are skipped entirely via `shouldNotFilter()` (checked against a list of `AntPathRequestMatcher`). This keeps the filter clean and avoids unnecessary token processing.
- **Token extraction** : looks for a header `Authorization: Bearer <token>`. If present, the username is extracted from the JWT using `JwtUtil`.
- **Validation** : the filter calls `jwtUtil.validateToken(token, userDetails)`, which checks that the username matches, the token is not expired, and the signature is correct.
- **Setting the security context** : if valid, a `UsernamePasswordAuthenticationToken` is placed in `SecurityContextHolder`, complete with the user’s granted authorities (roles). This ensures `@PreAuthorize` and method‑security checks work.
- **Error handling** : expired, malformed, or tampered tokens are logged and the filter simply continues (without setting authentication). The `AuthorizationFilter` will then reject the request if no authentication is present, resulting in a `401`.

#### JWT Utility (`JwtUtil`)

- **Token creation** : builds a signed JWT with claims: `sub` (username), `roles` (list of granted authorities), `iat`, `exp`. The secret is Base64‑decoded and used with HMAC‑SHA256.
- **Token validation** : extracts the username and expiration date; the token is valid if the username matches and it’s not expired.
- **Reloadable secret** : the secret is injected from `jwt.secret` (an environment variable), making it safe to rotate without code changes.

#### OAuth2 Login Flow

The application supports GitHub and Google OAuth2 out of the box.

1. **User clicks “Sign in with GitHub/Google”** : the browser is redirected to the provider’s authorization page.
2. **Provider callback** : the provider redirects back to `/login/oauth2/code/{provider}`. Spring Security intercepts this and calls `MyOAuth2UserService`.
3. **`MyOAuth2UserService`** : extends `DefaultOAuth2UserService`. It loads the OAuth2 user’s attributes, then delegates to an `OAuth2UserInfo` factory (`GoogleOAuth2UserInfo` or `GithubOAuth2UserInfo`) to extract a standardised set of fields (id, name, email, avatar).
   - If the user already exists by email, their record is updated (name, avatar).
   - If the user does not exist, a new `User` entity is created with a random password, `ROLE_USER`, and the provider assigned.
   - A `MyUser` object (which implements both `UserDetails` and `OAuth2User`) is returned as the authenticated principal.
4. **`OAuth2AuthenticationSuccessHandler`** : instead of redirecting to a default page, this handler:
   - Extracts the user’s principal (`MyUser` or `OAuth2User`).
   - Generates a JWT using `JwtUtil`, embedding the username and authorities.
   - Builds a redirect URL with the token as a query parameter:
     `https://fwaltnews.com/v1/news?token=eyJ...`
   - The browser follows the redirect; the React frontend detects the token in the URL, stores it in `localStorage`, and then navigates to the main page.
   - If the user is brand‑new, a `persistNewUser()` method ensures the user record is saved before generating the token (gracefully handling cases where the userinfo endpoint gives an email but no existing account).

#### Local authentication (email/password)

- `MyUserDetailsService` loads user details by username or email (useful for login). It returns a `MyUser` (which implements `UserDetails`).
- A `DaoAuthenticationProvider` is configured with a `BCryptPasswordEncoder`.
- The `/api/v1/auth/login` endpoint receives credentials, Spring’s `AuthenticationManager` authenticates them, and if successful, a JWT is generated and returned in the response body.

#### Why this design?

- **Stateless** : no server memory, easy to scale horizontally.

- **Hybrid** : combines the convenience of OAuth2 with the security of JWTs; the OAuth2 dance is done once, and all subsequent requests use the lightweight token.

- **Role‑based access** : the JWT contains roles, which Spring Security maps to `ROLE_USER`/`ROLE_ADMIN`; method‑level `@PreAuthorize` further protects sensitive operations.

- **Clean separation** : the authentication filter and OAuth2 success handler are self‑contained, making the code easier to test and maintain.

  

### Custom Validation 

Instead of relying solely on `jakarta.validation` (Bean Validation) with Hibernate Validator, this project includes a lightweight, fully custom validation engine. It was built to:

- Keep validation logic centralized and testable
- Support custom constraints that are specific to the application (e.g., `@SortAndOrder`, `@SearchCriteria`)
- Work independently of any external library. It's a pure Java reflection, plus Spring Dependency Injection

#### How it works

1. **Annotations** : Each constraint is a custom annotation (`@NotNull`, `@Size`, `@Min`, `@Max`, `@SortAndOrder`, `@SearchCriteria`). They are meta‑annotated with `@Constraint`, which marks them as validation rules.
2. **Constraint Checkers** : For each annotation, a Spring‑managed bean implements `ConstraintChecker<T>`. For example:
   - `MaxConstraintChecker` checks that a numeric value doesn’t exceed the limit.
   - `SizeConstraintChecker` verifies string length boundaries.
   - `SearchCriteriaChecker` parses raw filter strings and validates that each segment uses a known `SearchOperation`.
   - `SortAndOrderChecker` confirms that sort parameters are well‑formed and use valid directions.
3. **Validator Engine (`ValidatorImpl`)** : The core engine scans any object’s fields via reflection. For each field that carries an annotation annotated with `@Constraint`, it finds the matching `ConstraintChecker` from a pre‑built map, invokes `check()`, and collects `ConstraintViolation` objects if the check fails. It also recursively validates nested objects.
4. **Integration** : An AOP aspect (`ValidationAspect`) intercepts service methods whose parameters are annotated with `@Valid`, runs the validator before the method executes, and throws a `ValidatorException` if any violations are found. That exception is then handled by `RestExceptionHandler` (returning a `400 Bad Request`).

When a service method parameter like `@Valid ResourceSearchFilterRequestDTO dto` is processed, the aspect automatically calls `validator.validate(dto)`. If any of the filter strings are syntactically invalid or the page number is ≤ 0, the client receives a detailed error response.

#### Why this approach?

- **Extensible** : adding a new constraint is as simple as defining an annotation, marking it with `@Constraint`, and implementing a `ConstraintChecker` bean. No XML, no annotation processor.
- **Framework‑agnostic** : it doesn’t depend on a specific validation provider, so it can be adapted to any Java project.
- **Testable** : each checker is a unit‑testable Spring bean; the validator engine can be tested in isolation.
- **Explicit** : the validation logic isn’t hidden behind magic; developers can trace exactly how a rule is enforced.

This custom layer works alongside Spring’s `@Validated` on controllers (for basic DTO binding) but takes over the deeper business‑logic validation where we need application‑specific rules.

### Automated news pipeline

A Spring `@Scheduled` task in `NewsFetcherService` fetches fresh articles daily. It calls the [NewsData.io](https://newsdata.io/) API , which builds a request with the API key, language filter, and page size. The response is deserialized into a list of NewsDataItem records. Each item is then processed:

1. **Deduplication** : The method checks if an article with the same title already exists in the database). Duplicates are skipped.
2. **Full‑body scraping** : `ArticleScraper`, which uses **Jsoup** with a realistic browser user‑agent and a cascade of CSS selectors (`article`, `[itemprop=articleBody]`, `.article-body`, etc.) to extract the main content. If no selector yields enough text, it falls back to the API’s content or description field.
3. **Persisting** : A new `News` entity is created, linked to its author (looked up or created) and tags (each category becomes a `Tag`). The final body is stored in the database.
4. **Asynchronous embedding** : After saving, `embeddingService.embedNews(news.getId())` generates a 768‑dimensional vector via Google Gemini and stores it in the `news_embeddings` table for later semantic search. This runs asynchronously to avoid blocking the ingestion pipeline.

A separate scheduled job runs daily at 23:00 and deletes all articles older than 30 days. It also evicts the relevant caches (`@CacheEvict`) to keep the dataset fresh.

The entire pipeline is logged, and scraping failures are handled gracefully. If a page cannot be scraped, the system falls back to the API‑provided content without interrupting the flow.

#### **A note on scraping quality**

Because I’m on the free tier and don’t have access to premium news APIs with clean full-text, the current scraper uses generic CSS selectors to extract article text. This sometimes pulls in unwanted boilerplate like social share buttons, newsletter signup forms, related posts, and comment sections, especially when the target site’s layout is complex or doesn’t explicitly mark the article body. The result can be noisy content for some articles.

#### **Room for improvement**

A future iteration could replace the static selector cascade with a real content-extraction library (e.g., Mozilla’s Readability, or a Java port like `readability4j`), which would reliably isolate the main article text regardless of the site’s design. Alternatively, upgrading to a paid [Newsdata.io](https://newsdata.io/) plan (which returns full cleaned content) would eliminate scraping entirely and make the pipeline much more robust.



### Reliability & Data Integrity Fixes

Once the application was live with two Fargate replicas, the CloudWatch logs revealed a consistent error:

Couldn't save article '...':Query did not return a unique result: 2 results were returned

Multiple `Fetched and saved 0 new articles` entries meant the scheduled fetch was repeatedly hitting the same bottleneck.

#### Root cause

Two ECS tasks ran `NewsFetcherService.fetchLatestNews()` simultaneously. Inside the persistence logic, authors and tags were looked up by `findByName()` in `NewsPersistence.persist()`. If the row didn’t exist, both tasks tried to insert it at the same fraction of a second,  creating duplicate rows. Later, `findByName()` returned two rows instead of one, causing the uniqueness error and blocking the entire save operation.

Additionally, the `@Async` embedding was not actually running asynchronously, and the scraper was holding database connections open for seconds while waiting for external websites.

#### What was changed

| Problem                                                      | Solution                                                     |
| :----------------------------------------------------------- | :----------------------------------------------------------- |
| Duplicate authors/tags/news cause “not a unique result” errors    | Added `UNIQUE` constraints on `author.name`, `tag.name`, and `news.title` and cleaned up existing duplicates with a SQL script |
| Scraper held database transactions open for up to 5 seconds  | Moved `scraper.scrape()` out of the `@Transactional` block, scrape first, then save quickly |
| `@Async` on `embedNews` was bypassed by `this.embedNews()` inside the same class | Injected `ObjectProvider<EmbeddingService>` to call through the Spring proxy, restoring real asynchronous execution |
| Embedding fired before the database transaction committed, so the new row wasn’t visible yet | Registered a `TransactionSynchronization.afterCommit()` hook to trigger embedding only after the data is safely written |
| Multiple Fargate replicas executed the same scheduled job simultaneously, causing duplicate work and DB contention | Integrated `ShedLock` with a PostgreSQL‑backed lock table,  only one task can execute the fetch at a time across the whole cluster |



### Dynamic Search & Filtering

All list endpoints (`GET /api/v1/news`, `/api/v1/authors`, `/api/v1/tags`, `/api/v1/comments`) accept optional query parameters to filter, sort, and paginate results on the fly. Behind the scenes, a custom JPA Criteria API layer converts those parameters into type‑safe SQL queries, no hard‑coded repository methods needed.

#### How it works

1. **`SearchCriteria`** : a simple value object that captures a single filter condition:
   - `field` : the entity attribute (e.g., `title`, `author.name`)
   - `operation` : the comparison operator (`eq`, `like`, `gt`, `in`, `between`, etc.)
   - `value` : the value to compare against
   - `predicate` : optional logical connector (`and` / `or`) for chaining
2. **`SearchOperation`** : an enum that translates each operator into the correct JPA `CriteriaBuilder` expression. Supported operators include:
   - `eq`, `neq`, `gt`, `ge`, `lt`, `le`
   - `like`, `startlike`, `endlike`
   - `in`, `not`
   - `between`
   - Logical combinators `and` / `or`
3. **`SearchFilterSpecification<T>`** : implements Spring Data’s `Specification<T>` interface. For each `SearchCriteria`, it builds a `Predicate`:
   - If the field is `"keyword"`, it performs a global keyword search across `title` and `content` using `LIKE` with `%pattern%`.
   - If the field contains a dot (e.g., `author.name`), it automatically joins the related entity before applying the condition.
4. **`SearchFilterSpecificationsBuilder<T>`** : collects multiple `SearchCriteria` and chains them together with `AND` / `OR` logic. The final `Specification<T>` is used by the repository.
5. **`EntitySearchSpecification<T>`** : a container that bundles the filter `Specification<T>`, pagination (`Pagination`), and sorting (`Sorting`) into a single object. Built with a fluent builder pattern.

#### Example: API call to backend logic

A request like:

```
GET /api/v1/news?page=0&size=10&sort=title,asc&search=title:like:spring,content:like:boot
```

is parsed into:

- `Pagination`: page 0, size 10
- `Sorting`: `title` ascending
- `SearchCriteria`: `title LIKE %spring%` AND `content LIKE %boot%`

The controller passes these to the service, which builds an `EntitySearchSpecification<News>` using the builder. The service then calls `newsRepository.findAll(specification, pageable)`, and Spring Data JPA translates everything into a single efficient SQL query.

#### Why this matters ?

- **Zero boilerplate**: adding a new filterable field requires no new repository method, just use the existing `Specification` support.

- **Type‑safe**: all criteria are validated against the entity model; operators are restricted to the enum.

- **Composable**: complex filters can be built from simple key‑value query parameters, supporting nested associations and logical grouping.

- **Performance**: the generated SQL uses parameterised queries and benefits from database indexes.

  

### API Versioning

All endpoints are exposed under the path prefix `/api/v{apiVersion}/...` (e.g., `/api/v1/news`). The version number is not hard‑coded in every controller. Instead, a custom annotation and request matching logic handle version resolution elegantly.

#### How it works

1. **`@ApiVersion` annotation** : placed on a controller class or individual method. It defines the API version that the controller supports (defaults to `1`).
2. **`ApiVersionCondition`** : Spring MVC’s `RequestCondition` implementation. It inspects the incoming request URI, extracts the version from the `/v{number}/` segment using a regex, and compares it with the version declared by the `@ApiVersion` annotation.
   - If the versions match, the request proceeds.
   - If the version in the URI does not match, the application throws an `ApiVersionNotSupportedException`, which is translated into a clear error response.
3. **`ApiVersionRequestMappingHandlerMapping`** : a custom `RequestMappingHandlerMapping` that applies the `ApiVersionCondition` to every request mapping. It checks for the `@ApiVersion` annotation on the handler type (class) and method.
4. **`WebMvcRegistrationsConfig`** : registers the custom handler mapping so Spring uses it instead of the default one.

#### Usage example

All controller paths are defined using a constant prefix that includes the version placeholder:

```
public static final String API_ROOT_PATH = "/api/v{apiVersion}";
public static final String NEWS_API_ROOT_PATH = API_ROOT_PATH + "/news";
```

A controller then looks like:

```
@RestController
@RequestMapping(RestApiConst.NEWS_API_ROOT_PATH)
@ApiVersion(1)
public class NewsController { ... }
```

This means the controller responds to requests like `/api/v1/news`, `/api/v1/news/42`, etc. If someone tries `/api/v2/news`, they receive an error stating that version `2` is not supported.

#### Why this approach ?

- **Clean separation** : versioning logic is isolated in a few classes, not scattered across the codebase.
- **No path duplication** : the same controller can serve different versions by adding a second method with `@ApiVersion(2)` and adjusting the response, without changing the base path.
- **Future‑proof** : when you eventually need a `v2` API, you can introduce it without breaking existing clients, and the framework will automatically route requests to the correct version.
- **Self‑documenting** : the annotation clearly signals which version a controller implements.



### HATEOAS, Hypermedia-Driven API

The REST API follows the HATEOAS (Hypermedia As The Engine Of Application State) principle: every response contains not only the requested data but also links that tell the client what actions it can take next. This makes the API self‑describing so the frontend  can navigate the API without hardcoding URLs.

#### How it works

1. **`LinkBuilderUtil`** : a central utility that constructs absolute URLs for any controller endpoint.
   - It reads the current request context to get the base URL and API version.
   - It looks up the controller's `@RequestMapping` path and the `@ApiVersion` annotation (if present) to build the correct URL, replacing `{apiVersion}` with the actual version number.
   - It can build links for collection resources, single resources, nested resources (e.g., `/news/5/comments`), and specific methods (for different actions).
2. **Model Assemblers**  implement Spring HATEOAS's `RepresentationModelAssembler`. They take a plain DTO and enrich it with links:
   - `self` is the URL of the resource itself
   - related resources (`author`, `tags`, `comments`)
   - actions (`update`, `delete`)
   - collection‑level links (`news`) and creation endpoint
     The assembler also recursively calls other assemblers (e.g., `AuthorModelAssembler`) so that nested objects have their own links.
3. **`PageModelAssembler`**  enhances paginated responses (`PageDtoResponse<T>`) with navigation links:
   - `first`, `last`, `prev`, `next`
   - `self` for the current page
     These links preserve all query parameters (filters, sorting, page size) so the client can paginate without rebuilding the query string.

#### Why this approach ?

- **Discoverability** : A client can see all available transitions without documentation.
- **No hardcoded URLs** : The frontend can store only the root endpoint; all other paths are discovered from links.
- **Version‑safe** : Links automatically respect the current API version thanks to the version extraction logic.
- **Maintainability** : Changing a controller's path only requires updating the controller's annotation; all generated links update automatically.

### Centralized Error Handling

Instead of scattering `try-catch` blocks throughout the controllers, the application uses a single `@ControllerAdvice` class (`RestExceptionHandler`) that intercepts all unhandled exceptions and returns a consistent JSON error response.

Every exception thrown in the application (from controllers, services, or security) is caught by a dedicated `@ExceptionHandler` method. Also each handler maps the exception to a custom error code, a user‑friendly message, an optional technical detail, and the appropriate HTTP status. The error response is always serialized as JSON in the body, so the React frontend can parse it programmatically.

#### Handled exceptions (subset)

| Exception                          | HTTP Status               | Meaning                                |
| :--------------------------------- | :------------------------ | :------------------------------------- |
| `NotFoundException`                | 404 Not Found             | Resource does not exist                |
| `BadCredentialsException`          | 401 Unauthorized          | Incorrect username or password         |
| `AccessDeniedException`            | 403 Forbidden             | Insufficient permissions               |
| `ValidatorException`               | 400 Bad Request           | Invalid input payload                  |
| `ResourceConflictServiceException` | 409 Conflict              | Duplicate resource or state conflict   |
| `ApiVersionNotSupportedException`  | 503 Service Unavailable   | Requested API version is not supported |
| `AiIntegrationException`           | 500 Internal Server Error | AI embedding or chat call failed       |
| `Exception` (catch‑all)            | 500 Internal Server Error | Unexpected server error                |

All error codes and messages are centralized in an `ErrorCode` enum, making it easy to maintain consistency and add new error types.

### Caffeine caching

Every read-heavy service method is annotated with `@Cacheable`/`@CacheEvict`/`@CachePut`. Write operations maintain cache consistency automatically.

| Cache                 | TTL    | Evicted when                       |
| --------------------- | ------ | ---------------------------------- |
| `news` (by id)        | 10 min | create / update / delete           |
| `newsPage` (listings) | 5 min  | any news write                     |
| `authors`             | 30 min | any author write                   |
| `tags`                | 30 min | any tag write                      |
| `comments`            | 15 min | comment write — also evicts `news` |

I deliberately chose Caffeine as the in‑process cache for this project as an in‑memory cache gives blazing‑fast lookups with zero network hops and zero additional infrastructure with no separate Redis server to manage or pay for.  

I’m fully aware that with multiple replicas, each Fargate task holds its own independent cache. That means cache invalidation isn’t shared and some data could become stale across instances. For a small‑scale portfolio app, i felt like this is an acceptable compromise.  

Redis is the natural next step If the service ever scaled to more replicas or needed real‑time cache coherence.  I would introduce a dedicated ElastiCache Redis cluster to hold a single source of truth for cached data. Implementing Redis was originally in the plan, but I prioritised shipping a working end‑to‑end system first, and Caffeine gave me all I needed at no extra cost. I now have a clear mental model of when and why to make that transition.



### Testing

Testing is done at multiple layers, from unit tests up to end‑to‑end integration tests with a real database. The entire suite runs inside the GitHub Actions pipeline before any Docker image is built, ensuring nothing broken reaches production.

#### Integration Tests with a Real Database (Testcontainers)

All integration tests spin up a real PostgreSQL 17 instance with `pgvector` using Testcontainers. There is no in‑memory database, the tests connect to the exact same database engine that runs in production, so schema migrations, vector operations, and repository behaviour are identical.

- **Repository layer** (`BaseRepositoryTest`) starts a container and runs Flyway migrations, then tests JPA queries and relationships (e.g., saving a `News` with an `Author` and `Tags`). Assertions are done with **AssertJ** .
- **Service layer with caching** (`CaffeineServiceCacheTest`) uses mocked repositories to verify that repeated reads hit the cache instead of the database, and that mutating operations evict the correct cache entries.

#### AI Feature Tests

The AI service tests (`AiAssistantServiceTest`) also use a real `pgvector` container but mock the external Gemini API (`MyEmbeddingClient`). This allows validating the full embedding‑storage‑and‑retrieval flow without calling Google’s servers:

- The container starts with a custom SQL script to create tables and enable the `vector` extension.
- A test stores a 768‑dimensional vector for a news article, then the `EmbeddingService` is invoked and the stored vector is verified.
- The `AiAssistantService` is tested with a mocked `ChatClient.Builder` that simulates Gemini’s chat responses, ensuring the tool calling and message history logic works correctly.

#### Controller / API Tests (REST Assured + JWT)

The web layer is tested with REST Assured and Spring Boot’s random‑port test environment (`BaseControllerTest`). A real PostgreSQL container is used, tables are cleaned before each test, and JWT tokens are obtained from the login endpoint to authenticate requests. This exercises the full Spring Security filter chain, including role‑based access control.

#### OAuth2 Flow Tests

OAuth2 redirects are tested with MockMvc (`OAuth2Test`). A real database container is started, but the OAuth2 provider clients are mocked. The test verifies that after a successful Google or GitHub login, the application returns a redirect response (with a JWT token embedded), exactly as the production success handler does.

#### Unit Tests

Unit tests cover individual components (mappers, utilities, service methods) with all dependencies mocked or stubbed. These tests run in milliseconds and validate business logic in isolation. Together with the integration tests, they provide a fast and reliable safety net.

#### Why Testcontainers?

Testcontainers removed the need for a separate shared database environment. Every test class gets a clean, disposable PostgreSQL container. That means:

- Tests can run anywhere (local machine, CI) with zero manual setup.
- The database engine, extensions, and behaviour match production exactly.
- No test pollution, each test class starts fresh.



## Tech stack

### Backend

| Concern             | Technology                                                   |
| ------------------- | ------------------------------------------------------------ |
| Language / Runtime  | Java 17                                                      |
| Framework           | Spring Boot 3.3.0                                            |
| Security            | Spring Security  · JWT (JJWT 0.12.5) · OAuth2 Client         |
| Data access         | Spring Data JPA · Hibernate · Flyway 10.10.0                 |
| Database            | PostgreSQL 17 + pgvector                                     |
| AI / Embeddings     | Spring AI 1.1.5 · Gemini 3.1 Flash Lite · gemini-embedding-001 |
| Caching             | Caffeine 3.1.8                                               |
| API style           | REST · Spring HATEOAS · Springdoc OpenAPI 3                  |
| News scraping       | Jsoup · NewsData.io                                          |
| Monitoring(locally) | Micrometer · Prometheus · Grafana                            |
| Build               | Gradle 8.14 (multi-module)                                   |
| Code quality        | SonarCloud · JaCoCo · Spotless                               |

### Frontend

| Concern          | Technology        |
| ---------------- | ----------------- |
| Framework        | React 18 (CRA)    |
| State management | Redux Toolkit     |
| HTTP             | Axios             |
| Routing          | React Router v6   |
| UI               | React Bootstrap 5 |
| Hosting          | Cloudflare Pages  |

### Infrastructure & DevOps

| Concern            | Technology                                                   |
| ------------------ | ------------------------------------------------------------ |
| Cloud provider     | AWS                                                          |
| Networking         | VPC (public + private subnets, Internet Gateway)             |
| Compute            | ECS Fargate (2 replicas, public IPs)                         |
| Database           | RDS PostgreSQL 17 (Single AZ, pgvector)                      |
| Frontend CDN       | Cloudflare Pages (global CDN, free tier)                     |
| Load balancing     | Application Load Balancer (HTTP listener)                    |
| DNS + TLS          | Cloudflare (DNS management, flexible SSL termination)        |
| Container registry | ECR                                                          |
| IaC                | Terraform                                                    |
| CI/CD              | GitHub Actions                                               |
| Monitoring         | CloudWatch Logs (7‑day retention) + Prometheus metrics exposed |
| Secrets management | AWS Parameter Store (SecureString)                           |

---

## Running locally

### Prerequisites

- Docker Desktop
- Java 17
- Node 18+

### 1. Clone

```bash
git clone https://github.com/Fwalt237/FwaltNews.git
cd FwaltNews
```

### 2. Create `.env`

The backend needs a few secrets to connect to the database, sign JWT tokens, and call external APIs.
Those secrets are never stored in the code, they’re loaded from a `.env` file that you create on your own machine.

```bash
touch .env        # create an empty .env file
nano .env         # open it in the Nano editor to paste your variables
# or use:
vi .env           # open it in Vim if you prefer it

```

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/yourdbname
DB_PASSWORD=*******
JWT_SECRET=*******
GEMINI_API_KEY=*******
GITHUB_CLIENT_ID=*******
GITHUB_CLIENT_SECRET=*******
GOOGLE_CLIENT_ID=*******
GOOGLE_CLIENT_SECRET=*******
OAUTH2_REDIRECT_URI=http://localhost:3000/v1/news
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
NEWSDATA_API_KEY=*******
```

| Variable                                    | How to get a real value                                      |
| :------------------------------------------ | :----------------------------------------------------------- |
| `DB_PASSWORD`                               | Make up a strong password. Used for the local PostgreSQL container. |
| `JWT_SECRET`                                | A random Base64 string. Generate with `openssl rand -base64 32` or type any 64‑character string. |
| `GEMINI_API_KEY`                            | Create an API key in the Google Cloud Console (APIs & Services, under Credentials). Enable the Generative Language API. |
| `GITHUB_CLIENT_ID` / `GITHUB_CLIENT_SECRET` | Register an OAuth App in GitHub Developer Settings. Set the callback URL to `http://localhost:8080/login/oauth2/code/github`. |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Register an OAuth 2.0 Web Client in Google Cloud Console. Add `http://localhost:8080/login/oauth2/code/google` as an authorised redirect URI. |
| `NEWSDATA_API_KEY`                          | Register at [newsdata.io](https://newsdata.io/) and copy your free API key. |

### 3. Start

```bash
docker compose up --build
```

Once everything is running:

| Service    | URL                                              |
| ---------- | ------------------------------------------------ |
| Frontend   | http://localhost:3000                            |
| Backend    | http://localhost:8080                            |
| Swagger    | http://localhost:8080/swagger-ui/index.html      |
| Prometheus | http://localhost:9091                            |
| Grafana    | http://localhost:3001 — login: `admin` / `admin` |

### 4. Promote to admin (once)

Connect to the local database with any PostgreSQL client (host: `localhost`, port: `5432`, user: `postgres`, password: the one you set in `.env`) and run:

```sql
INSERT INTO user_roles (user_id, role)
VALUES ((SELECT id FROM users WHERE username = 'your_username'), 'ROLE_ADMIN');
```



### 5. Tests

```bash
cd backend

# All modules (requires Docker — Testcontainers spins up pgvector)
./gradlew test

# Individual modules
./gradlew :module-repository:test
./gradlew :module-service:test
./gradlew :module-web:test
```



## CI/CD pipeline

The project uses GitHub Actions to automate the entire backend delivery process from code push to production. Any push to the `main` branch that touches files inside backend/** or the workflow file itself starts the pipeline.This avoids unnecessary builds when only the frontend or documentation changes.

### Pipeline Stages

The workflow is split into two jobs:

#### 1. `build-and-scan`

- **Checkout** : pulls the full repository.

- **JDK 17 Setup** : installs Temurin JDK with Gradle caching.

- **Build & Test** : runs `./gradlew build`, which compiles the code, executes unit and integration tests (including Testcontainers), and applies code quality checks like Checkstyle and Spotless. If any test fails, the pipeline stops here. No broken code reaches production.

- **SonarQube Scan**  : performs a static code analysis on SonarCloud.

  At this stage, the SonarCloud Quality Gate fails because the overall test coverage is ~36.6%, well below the 80% threshold.  I chose to keep the pipeline flowing anyway by setting `continue-on-error: true`. This was a deliberate trade‑off: my first goal was to get a fully working, end‑to‑end system into production, something I could show and talk about in interviews. 
  The tests that are in place(166) cover the most critical paths (repository logic, caching, REST API, AI embedding storage, OAuth2 flow, etc..), and I’m actively adding more tests to reach the target coverage. 
  For now, the quality gate acts as a visible “to‑do list” rather than a release blocker, it’s a reminder that the job isn’t done, but it doesn’t stop the app from delivering value.

#### 2. `deploy` (runs only if `build-and-scan` succeeded)

- **Checkout** : again pulls the source (needed for Docker build).
- **AWS Credentials** : authenticates to AWS using the `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` stored as GitHub Secrets.
- **Login to ECR** : authenticates Docker to the private container registry.
- **Docker Build & Push** : builds a multi‑stage Docker image from the `Dockerfile` in `./backend`, tags it as `latest`, and pushes it to Amazon ECR.
- **Force ECS Deployment** : tells the existing ECS service to pull the new `latest` image and replace the running tasks. Because the service is configured with 2 replicas and a health‑check endpoint, the update happens without downtime.

### GitHub Secrets 

| Secret                  | Purpose                                                 |
| :---------------------- | :------------------------------------------------------ |
| `AWS_ACCESS_KEY_ID`     | IAM user access key for pushing images and updating ECS |
| `AWS_SECRET_ACCESS_KEY` | Corresponding secret key                                |
| `SONAR_TOKEN`           | Token for SonarCloud analysis                           |

All application secrets (database password, JWT secret, API keys) are stored exclusively in AWS Parameter Store and are never exposed to the CI/CD pipeline.

### Frontend Pipeline

The React frontend is deployed automatically via Cloudflare Pages. On every push to `main`, Cloudflare detects changes in the `frontend/` folder, runs `npm install && npm run build`, and deploys the static assets to a global CDN. No additional GitHub Actions configuration was needed.



## Monitoring

The application has two separate monitoring strategies: a local stack for development, and a production‑grade AWS CloudWatch setup that runs 24/7 at no extra cost.

#### Local development (Docker Compose)

When run `docker compose up`, a **Prometheus** server and a **Grafana** instance are started automatically:

- Prometheus scrapes `http://backend:8080/actuator/prometheus` every 15 seconds.
- Grafana is available at `http://localhost:3001` (credentials: `admin` / `admin`).

This stack is only used for experimenting and debugging locally.

#### Production monitoring (AWS CloudWatch)

In the AWS environment, all observability is handled by CloudWatch, which is enabled by default and stays within the free tier.

##### Logs

All application logs (Spring Boot console output) are sent to the CloudWatch log group `/ecs/fwaltnews-backend` with a 7‑day retention period. It can be viewed via the AWS Console or run real‑time queries with CloudWatch Logs Insights.

Example query fetching the last 50 fetch‑related logs:

fields @timestamp, @message
| filter @message like /News fetch/ or @message like /Fetched and saved/ or @message like /Couldn't save article/
| sort @timestamp desc
| limit 50

##### Metrics

AWS automatically collects metrics for every service used. The most important ones are:

- **ECS tasks** : CPU utilisation, memory utilisation
- **Application Load Balancer** : request count, target response time, HTTP 4xx/5xx counts
- **RDS** : database connections, CPU, free storage space

These can be visualised directly in the AWS Console under **CloudWatch, under Metrics**. No additional agents or setup is required.

##### Health checks

The ALB target group uses the endpoint `/actuator/health` to verify that each Fargate task is healthy. If a task fails three consecutive checks, it is automatically replaced.

##### Future enhancement

The `/actuator/prometheus` endpoint is still active in production. It can be scraped by a managed Grafana instance (e.g., Grafana Cloud’s free tier) or the CloudWatch agent for richer dashboards without changing any code.



## Frontend (React + Redux + Cloudflare Pages)

The frontend is a single‑page application built with Create React App, styled with Bootstrap 5, and deployed to Cloudflare Pages for zero‑cost, global CDN hosting.

### Routing & layout

`App.jsx` defines all routes using React Router v6. The main content pages are wrapped in a shared `Layout` component that includes the header, footer, and an AI chat widget (`ChatWidget`). The root `/` and any unknown paths redirect to `/v1/news`.

Protected routes (`/v1/news` for authenticated actions) are wrapped in a `ProtectedRoute` component that checks for a valid JWT token in Redux state and in URL query parameters (to support OAuth2 redirects with tokens).

### State management (Redux Toolkit)

Two main slices manage global state(`store.js`):

- **`authSlice`** : stores the JWT token and decoded user info. On login/signup success, the token is decoded with `jwt-decode`, and both token and user are persisted in `localStorage`. The `logout` action clears state and storage.
- **`newsSlice`** : manages the list of news articles, the currently viewed article, pagination metadata, and CRUD async thunks. All API calls go through the central Axios instance.

### API communication (Axios)

A single Axios instance (`api.js`) is configured with:

- **Base URL** : reads `REACT_APP_API_URL` from environment variables (set to `https://api.fwaltnews.com/api` in production, `http://localhost:8080/api` locally). Falls back to `/api` for relative paths.
- **Request interceptor** : attaches the `Authorization: Bearer <token>` header from `localStorage` to every request.
- **Response interceptor** : on a 401 response, clears the stored token and user data (auto‑logout on session expiry).

### Data fetching pattern (Custom hooks)

Data fetching logic is extracted into custom hooks for reusability. For example, `useNewsDetail(id)` fetches a single article and its comments in parallel, exposes `loading` / `error` state, and provides a `postComment` function that optimistically updates the local comment list.

### Local development & production builds

- **Local dev** : `npm start` uses `react-scripts` dev server. CORS is configured on the backend for `http://localhost:3000`.
- **Docker local environment** : a multi‑stage Dockerfile builds the React app and serves it with Nginx. The Nginx config uses `try_files $uri /index.html` to support client‑side routing (all paths fall back to `index.html`).
- **Production** : Cloudflare Pages builds the app with `npm run build`. The build output (`build/`) is deployed to Cloudflare’s CDN. Environment variable `REACT_APP_API_URL=https://api.fwaltnews.com/api` is set in the Pages dashboard.
- **Legacy peer dependencies** : a `.npmrc` file with `legacy-peer-deps=true` ensures that `react-scripts@5` installs cleanly despite peer dependency warnings in the dependency tree.

### AI Chat Widget

The chat widget (`ChatWidget`) calls the backend’s AI endpoint (`/api/v1/ai`) and renders the assistant’s responses, including article cards when the assistant references specific articles. It’s a simple integration that demonstrates how the frontend consumes a non‑trivial API.

Even though my primary focus is backend engineering, building this frontend gave me a solid understanding of modern React patterns, state management, API integration, and the full CI/CD flow for a static site, skills that make me a more effective back‑end developer and collaborator.





## What I could add next (and why I haven't yet)

| Feature                            | Why it’s on hold                                             |
| ---------------------------------- | ------------------------------------------------------------ |
| **Redis cache**                    | For now, Caffeine gives me sub‑millisecond lookups with zero extra infrastructure. Redis only becomes worth it when I need shared state across many more replicas. |
| **Auto‑scaling ECS**               | I’d need load‑test data first to set sensible CPU/memory thresholds. Guessing could waste money or cause instability. It’s on the roadmap once I have traffic patterns. |
| **Kubernetes (EKS)**               | This is my next big learning milestone. One of the MJC School senior mentor’s advice about service discovery and fault tolerance. That naturally leads here, but I wanted to master ECS first. |
| **Rate limiting( Bucket4j)**       | Would stop a single IP from hammering the API, especially useful for the free‑tier AI endpoints. I haven’t added it yet because the public load is tiny, but it's the next steps if the traffic increase. |
| **Circuit breaker (Resilience4j)** | External calls to NewsData.io and Gemini can fail. Right now I log and move on, but a proper circuit breaker would make those calls more graceful. A logical next step for production robustness. |



## About

Hi, I’m Foko Walter,  a career switcher with a background in civil engineering, now fully immersed in software engineering.

I chose Java and Spring not because they’re easy, but because they demand real engineering thinking: designing maintainable architectures, understanding concurrency, and owning the full lifecycle from code to cloud. This project, FwaltNews, is my answer to “show me what you can build”. I think it's a complete production‑grade system, that I designed, deployed, and debugged.

Every decision in this repo has a story behind it. I’d love to walk you through the architecture, the trade‑offs, and the lessons learned.

I’m actively looking for **entry‑level backend, DevOps, cloud, or full‑stack roles**. If you’re a recruiter, an engineer, or just someone curious about the project, feel free to reach out.

**Email:** fokowalter17@gmail.com
**GitHub:** [Fwalt237](https://github.com/Fwalt237)
**LinkedIn:** [Foko Walter](https://www.linkedin.com/in/foko-walter-b175653a8/?lipi=urn%3Ali%3Apage%3Ad_flagship3_profile_view_base_contact_details%3BlS0RFlsqT9qcYQ%2FqO8qSJg%3D%3D)

