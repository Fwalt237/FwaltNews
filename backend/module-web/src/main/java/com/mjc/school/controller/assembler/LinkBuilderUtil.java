package com.mjc.school.controller.assembler;

import com.mjc.school.versioning.ApiVersion;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.lang.reflect.Method;

@Component
public class LinkBuilderUtil{

    public String buildLink(Class<?> controllerClass, String methodName, Object... pathVariables){
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String currentVersion = extractApiVersionFromRequest();

        String apiVersion = getApiVersionFromController(controllerClass, methodName);
        if(apiVersion == null){
            apiVersion = currentVersion;
        }
        String basePath = getRequestMappingPath(controllerClass);

        basePath = basePath.replace("{apiVersion}",apiVersion);
        StringBuilder fullPath = new StringBuilder(baseUrl).append(basePath);

        if(pathVariables != null){
            for(Object pathVariable : pathVariables){
                fullPath.append("/").append(pathVariable);
            }
        }
        return fullPath.toString();
    }

    public String buildLink(Class<?> controllerClass, Object... pathVariables){
        return buildLink(controllerClass,null,pathVariables);
    }

    public String buildResourceLink(Class<?> controllerClass, Object id){
        return buildLink(controllerClass,null,id);
    }

    public String buildResourceLinkForMethod(Class<?> controllerClass,String methodName, Object id){
        return buildLink(controllerClass,methodName,id);
    }

    public String buildCollectionLink(Class<?> controllerClass){
        return buildLink(controllerClass,null);
    }

    public String buildCollectionLinkForMethod(Class<?> controllerClass,String methodName){
        return buildLink(controllerClass,methodName);
    }

    public String buildNestedResourceLink(Class<?> controllerClass,Object parentId,String nestedResource){
        String baseLink = buildResourceLink(controllerClass,parentId);
        return baseLink + "/" + nestedResource;
    }

    public String buildNestedResourceLinkForMethod(Class<?> controllerClass,String methodName,Object parentId,String nestedResource){
        String baseLink = buildResourceLinkForMethod(controllerClass,methodName,parentId);
        return baseLink + "/" + nestedResource;
    }

    private String extractApiVersionFromRequest(){
        try{
            String requestUri = ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUriString();
            if(requestUri.contains("/api/v")){
                int vIndex = requestUri.indexOf("/api/v") + 6;
                if(vIndex < requestUri.length()){
                    char versionChar = requestUri.charAt(vIndex);
                    if(Character.isDigit(versionChar)){
                        return String.valueOf(versionChar);
                    }
                }
            }
        }catch(Exception e){
            System.err.println("Warning: Could not extract API from request: "+e.getMessage());
        }
        return "1";
    }

    private String getApiVersionFromController(Class<?> controllerClass, String methodName){
        try{
            if (methodName!=null && !methodName.isEmpty()){
                for(Method method : controllerClass.getDeclaredMethods()){
                    if(method.getName().equals(methodName)){
                        ApiVersion methodVersion = AnnotatedElementUtils.findMergedAnnotation(method,ApiVersion.class);
                        if(methodVersion!=null){
                            return String.valueOf(methodVersion.value());
                        }
                        break;
                    }
                }
            }
            ApiVersion classVersion = AnnotatedElementUtils.findMergedAnnotation(controllerClass,ApiVersion.class);
            if(classVersion!=null){
                return String.valueOf(classVersion.value());
            }
        }catch(Exception e){
            System.err.println("Warning: Could not extract API version from "+
                    controllerClass.getName()+(methodName!=null ? "."+methodName : "")+
                    ": "+e.getMessage());
        }
        return null;
    }

    private String getRequestMappingPath(Class<?> controllerClass) {

        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(controllerClass, RequestMapping.class);

        if (requestMapping != null && requestMapping.value().length > 0) {
            return requestMapping.value()[0];
        }

        throw new IllegalStateException("Controller "+controllerClass.getSimpleName()+
                " must have @RequestMapping annotation with value");

    }
}
