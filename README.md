#WSO2 Enterprise Service Bus

WSO2 Enterprise Service Bus is a lightweight, high performance, near-zero latency product, providing comprehensive support for several different technologies like SOAP, WS* and REST as well as domain-specific solutions and protocols like SAP or HL7. It goes above and beyond by being 100% compliant with enterprise integration patterns. It also has 100+ ready-made, easy-to-use connectors to seamlessly integrate between cloud service providers. WSO2 Enterprise Service Bus is 100% configuration driven, which means no code needs to be written. Its capabilities can be extended too with the many extension points to plug into.

<h2>
Key Features of WSO2 Enterprise Service Bus
</h2>

<h3>
Connecting Anything to Anything
</h3>
<ul>
<li>
    Adapters to cloud services: 100+ connectors including Salesforce, Paypal, LinkedIn, Twitter and  JIRA,
</li>
<li>
    Transports: HTTP, HTTPS, POP, IMAP, SMTP, JMS, AMQP, FIX, TCP, UDP, FTP, FTPS, SFTP, CIFS, MLLP and  SMS
</li>
<li>
    Formats & protocols: JSON, XML, SOAP 1.1, SOAP 1.2, WS-*, HTML, EDI, HL7, OAGIS, Hessian, Text, JPEG, MP4, all binary formats and CORBA/IIOP
</li>
<li>
    Adapters to COTS systems: SAP BAPI & IDoc, PeopleSoft, MS Navision, IBM WebSphere MQ, Oracle AQ and MSMQ
</li>
<li>
    Inbound endpoints: HTTP, HTTPS, HL7, JMS, File, MQTT, Kafka, CXF WS RM and RabbitMQ
</li>
</ul>

<h3>Routing, Mediation & Transformation
</h3>
<ul>
<li>
    Routing: Header based, content based, rule-based and priority-based routing
</li>
<li>
    Mediation: EIPs (including scatter/gather, message filters, recipient list, dead-letter channels, guaranteed delivery and message enrichment), database integration, event publishing, logging & auditing, validation
</li>
<li>
    Transformation: XSLT 1.0/2.0, XPath, XQuery and Smooks
</li>
</ul>

<h3>Message, Service, API & Security Gateway
</h3>

<ul>
<li>
    Expose existing applications & services over different protocols and message formats
</li>
<li>
    Virtualize services for loose coupling and SOA governance
</li>
<li>
    Load balancing for scalability and failover for high availability of business endpoints
</li>
<li>
    Create service facades for legacy / non-standard services
</li>
<li>
    Enforce and manage security centrally, including authentication, authorization and entitlement
</li>
<li>
    Policy enforcement and governance via WSO2 Governance Registry
</li>
<li>
    Expose services & applications via RESTful APIs with key management
</li>
<li>
    Logging, audit and SLA monitoring, KPI monitoring
</li>
<li>
    WS-Security, LDAP, Kerberos, OpenID, SAML, XACML
</li>
<li>
    SSL tunneling and SSL profiles support for inbound and outbound scenarios
</li>
<li>
    CRL/OCSP Certificate revocation verification
</li>
</ul>


<h3>High Performance, High Availability, Scalability & Stability
</h3>
<ul>
<li>
    Supports 1000s of concurrent non-blocking HTTP(S) connections per server
</li>
<li>
    Pure streaming and on-demand processing of messages
</li>
<li>
    Sub-millisecond latency for high-throughput scenarios
</li>
<li>
    Supports highly available deployment
</li>
<li>
    Horizontal scaling via clustering with stateless server architecture
</li>
<li>
    Long term execution stability with low resource utilization
</li>
</ul>

<h3>Lightweight, Developer Friendly and Easy to Deploy
</h3>
<ul>
<li>
    Declarative development with configuration instead of code
</li>
<li>
    Easy configuration of fault tolerant mediations with support for error handling
</li>
<li>
    Server customization via feature provisioning of any WSO2 middleware capability
</li>
<li>
    Extend configuration language with custom DSLs via templates
</li>
<li>
    Embed scripting language code in Javascript, JRuby, Groovy and more as custom mediators
</li>
<li>
    Integrated with SVN, Maven, Ant and other standard tools for development & deployment
</li>
<li>
    Integrated to WSO2 Developer Studio, Eclipse-based IDE for all WSO2 products
</li>
</ul>

<h3>Manage & Monitor
</h3>
<ul>
<li>
    Comprehensive management & monitoring Web console with enterprise-level security
</li>
<li>
    Built-in collection and monitoring of standard access and performance statistics
</li>
<li>
    JMX MBeans for key metrics monitoring and management
</li>
<li>
    Integrates with WSO2 Data Analytics Server for operational audit and KPI monitoring and management
</li>
<li>
    Flexible logging support with integration to enterprise logging systems
</li>
<li>
    Centralized configuration management across different environments with lifecycles and versioning via integration to WSO2 Governance Registry
</li>
</ul>

<h2>Known Issues
</h2>
All the open issues pertaining to WSO2 Enterprise Service Bus are reported at the following location:

<a href='https://wso2.org/jira/issues/?filter=12394'>Known Issues</a>

<h2>How You Can Contribute
</h2>

<h3>Mailing Lists
</h3>
Join our mailing list and correspond with the developers directly.

Developer List : dev@wso2.org | <a href='dev-request@wso2.org?subject=subscribe'>Subscribe</a> | <a href='http://wso2.org/mailarchive/dev/'>Mail Archive</a>

<h3>Reporting Issues
</h3>
We encourage you to report issues, documentation faults and feature requests regarding WSO2 Enterprise Service Bus through the <a href='https://wso2.org/jira/browse/ESBJAVA'>public JIRA</a>. You can use the <a href='https://wso2.org/jira/browse/CARBON'>Carbon JIRA</a> to report any issues related to the Carbon base framework or associated Carbon components.

<h2>Support
</h2>
We are committed to ensuring that your enterprise middleware deployment is completely supported from evaluation to production. Our unique approach ensures that all support leverages our open development methodology and is provided by the very same engineers who build the technology.

For more details and to take advantage of this unique opportunity please visit http://wso2.com/support.

The project home page is http://wso2.com/products/enterprise-service-bus/

##Jenkins Build Status

|  Branch | Build Status |
| :------------ |:-------------
| product-esb master      | [![Build Status](https://wso2.org/jenkins/job/product-esb/badge/icon)](https://wso2.org/jenkins/job/product-esb)
