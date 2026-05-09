export default function AboutPage() {
  return (
    <div className="container-fluid" style={{ maxWidth: 1400, margin: '0 auto', padding: '0 24px', paddingTop: '80px' }}>
      <div className="page-header" style={{ marginBottom: 32 }}>
        <h1 className="page-title">About FwaltNews</h1>
        <p className="page-sub">Delivering the stories that matter.</p>
      </div>
      
      <div style={{ color: 'var(--text-primary)', lineHeight: 1.8, fontSize: 16, maxWidth: 800 }}>
        <p style={{ marginBottom: 16 }}>
          Welcome to FwaltNews. We are an independent news platform dedicated to providing 
          accurate, timely, and comprehensive coverage of global events, sports, tech, and beyond.
        </p>
        <p>
          Our mission is to cut through the noise and deliver high-quality journalism directly 
          to your screen. Whether you're tracking the latest policies or following your favorite team, 
          we've got you covered.
        </p>
      </div>

      <div style={{ marginTop: 60, borderTop: '1px solid var(--border)', paddingTop: 40 }}>
        <section id="tos" style={{ marginBottom: 40 }}>
            <h3 style={{ color: 'var(--text-primary)', fontSize: 20 }}>Terms of Service</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
                This is a portfolio project for demonstration purposes only. By using FwaltNews, 
                you acknowledge that this is a mock environment and no real contractual 
                obligations are created.
            </p>
        </section>

        <section id="pp">
            <h3 style={{ color: 'var(--text-primary)', fontSize: 20 }}>Privacy Policy</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
                We value your privacy. Note that in this demo version, data is stored in a 
                local/test database. Do not use real sensitive passwords. We do not sell 
                your data because, well, it's a mock.
            </p>
        </section>
      </div>
    </div>
  );
}