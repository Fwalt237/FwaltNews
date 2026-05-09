export default function ContactPage() {
  return (
    <div className="container-fluid" style={{ maxWidth: 1400, margin: '0 auto', padding: '0 24px', paddingTop: '80px' }}>
      <div className="page-header" style={{ marginBottom: 32 }}>
        <h1 className="page-title">Contact Us</h1>
        <p className="page-sub">We'd love to hear from you.</p>
      </div>

      <div style={{ color: 'var(--text-secondary)', lineHeight: 1.8, fontSize: 16, maxWidth: 800 }}>
        <p style={{ marginBottom: 24 }}>
          Have a news tip, a question about your account, or just want to send us some feedback? 
          Reach out using the details below:
        </p>

        <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
            <li style={{ marginBottom: 12 }}>
                <strong style={{ color: 'var(--text-primary)' }}>Email:</strong>{' '}
                <a 
                href="mailto:fokowalter17@gmail.com" 
                className="contact-link"
                >
                fokowalter17@gmail.com
                </a>
            </li>
            
            {/* You can reuse the class for other links too! */}
            <li style={{ marginBottom: 12 }}>
                <strong style={{ color: 'var(--text-primary)' }}>GitHub:</strong>{' '}
                <a 
                href="https://github.com/Fwalt237" 
                target="_blank" 
                rel="noopener noreferrer" 
                className="contact-link"
                >
                github.com/Fwalt237
                </a>
            </li>
        </ul>
      </div>
    </div>
  );
}