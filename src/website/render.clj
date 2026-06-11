(ns website.render
  "HTML rendering — pure functions from data to strings."
  (:require [website.content :as content]
            [clojure.string :as str]))

(defn- page-shell
  "Full HTML document wrapper with shared nav."
  [title body & {:keys [toc card-layout]}]
  (str "<!DOCTYPE html><html lang=\"en\"><head>"
       "<meta charset=\"utf-8\">"
       "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
       "<title>" title " — acestus.com</title>"
       "<link rel=\"stylesheet\" href=\"/static/css/style.css\">"
       "<script>((d)=>{const t=localStorage.getItem('theme')"
       "||(matchMedia('(prefers-color-scheme:dark)').matches?'dark':'light');"
       "d.setAttribute('data-theme',t)})(document.documentElement)</script>"
       "</head><body>"
       "<header class=\"site-header\">"
       "<a href=\"/\" class=\"site-name\">acestus.com</a>"
       "<nav class=\"site-nav\" aria-label=\"Site navigation\">"
       "<a href=\"/blog\">Blog</a>"
       "<a href=\"/portfolio\">Portfolio</a>"
       "<a href=\"/contact\">Contact</a>"
       "</nav>"
       "<button class=\"theme-toggle\" onclick=\"((d)=>{const n="
       "d.getAttribute('data-theme')==='dark'?'light':'dark';"
       "d.setAttribute('data-theme',n);localStorage.setItem('theme',n)})"
       "(document.documentElement)\" aria-label=\"Toggle theme\">"
       "<span class=\"icon-sun\">☀</span><span class=\"icon-moon\">☾</span>"
       "</button></header>"
       "<main class=\"" (cond card-layout "card-layout" toc "layout-with-toc" :else "layout-single") "\">"
       (when toc (str toc))
       (if card-layout body (str "<article class=\"content\">" body "</article>"))
       "</main>"
       "<footer class=\"site-footer\"><p>acestus.com</p></footer>"
       "</body></html>"))

(defn landing-page []
  (page-shell
   "Home"
   (str "<div class=\"card\">"
        "<img class=\"avatar-photo\" src=\"/static/photo.png\" alt=\"William Weeks-Balconi\">"
        "<h1 class=\"card-name\">William Weeks-Balconi</h1>"
        "<p class=\"card-bio\">Husband, father, Christian, historian, builder.<br>"
        "Into Clojure, Microsoft Fabric, and writing about Infrastructure as Code.<br>"
        "I prefer to be contacted by email.</p>"
        "<a class=\"add-contact-btn\" href=\"/static/contact.vcf\" download=\"william-weeks-balconi.vcf\"><span class=\"link-icon\">👤</span><span class=\"link-label\">Add to Contacts</span></a>"
        "<a class=\"link-btn\" href=\"https://portfolio.acestus.com\" target=\"_blank\" rel=\"noopener noreferrer\"><span class=\"link-icon\">🌐</span><span class=\"link-label\">Portfolio</span></a>"
        "<a class=\"link-btn\" href=\"https://fabric.acestus.com\" target=\"_blank\" rel=\"noopener noreferrer\"><span class=\"link-icon\">📊</span><span class=\"link-label\">Fabric</span></a>"
        "<a class=\"link-btn\" href=\"/contact\"><span class=\"link-icon\">✉</span><span class=\"link-label\">Contact</span></a>"
        "<a class=\"link-btn\" href=\"/blog\"><span class=\"link-icon\">✍</span><span class=\"link-label\">Blog</span></a>"
        "</nav>"
        "</div>")
   :card-layout true))

(defn- toc-html [toc]
  (when (seq (rest toc))
    (str "<nav class=\"toc\" aria-label=\"Table of contents\">"
         "<h2 class=\"toc-title\">Contents</h2><ol>"
         (str/join
          (for [{:keys [text anchor level]} (rest toc)
                :when (<= level 3)]
            (str "<li class=\"toc-" level "\">"
                 "<a href=\"#" anchor "\">" text "</a></li>")))
         "</ol></nav>")))

(defn blog-index-page [posts]
  (page-shell
   "Blog"
   (str "<section class=\"subscribe\">"
        "<p>Subscribe to get future posts via email "
        "(or grab the <a href=\"https://world.hey.com/jump/feed.atom\">RSS feed</a>)</p>"
        "<a class=\"subscribe-btn\" href=\"https://world.hey.com/jump\">Subscribe via email</a>"
        "</section>"
        "<h1>Posts</h1><ul class=\"post-list\">"
        (str/join
         (for [p (content/posts-by-date posts)]
           (str "<li class=\"post-item\">"
                "<a href=\"/blog/" (:slug p) "\">" (:title p) "</a>"
                "<time>" (subs (:date p) 0 10) "</time>"
                "<p>" (:summary p) "</p>"
                "</li>")))
        "</ul>")))

(defn post-page [post]
  (page-shell (:title post) (:html post) :toc (toc-html (:toc post))))

(def ^:private contact-links
  [{:label "Email"    :detail "jump@hey.com"                  :url "mailto:jump@hey.com"                  :icon "✉"}
   {:label "Blog"     :detail "world.hey.com/jump"            :url "/blog"                                :icon "✍"}
   {:label "Website"  :detail "acestus.com"                   :url "https://www.acestus.com"              :icon "🌐"}
   {:label "GitHub"   :detail "github.com/Acestus"            :url "https://github.com/Acestus"           :icon "⌨"}
   {:label "Mastodon" :detail "@acestus@social.linux.pizza"   :url "https://social.linux.pizza/@acestus"  :icon "🐘" :rel "me"}
   {:label "Telegram" :detail "t.me/acestus"                  :url "https://t.me/acestus"                 :icon "✈"}
   {:label "LinkedIn" :detail "linkedin.com/in/acestus"       :url "https://linkedin.com/in/acestus"      :icon "💼"}])

(defn- link-item [{:keys [label detail url icon rel]}]
  (let [rel-val (cond
                  rel                          (str "noopener noreferrer " rel)
                  (str/starts-with? url "http") "noopener noreferrer"
                  :else                        nil)]
    (str "<a class=\"link-btn\" href=\"" url "\""
         (when rel-val (str " rel=\"" rel-val "\""))
         "><span class=\"link-icon\">" icon "</span>"
         "<span class=\"link-text\">"
         "<span class=\"link-label\">" label "</span>"
         "<span class=\"link-detail\">" detail "</span>"
         "</span></a>")))

(defn contact-page []
  (page-shell
   "Contact"
   (str "<div class=\"card\">"
        "<img class=\"avatar-photo\" src=\"/static/photo.png\" alt=\"William Weeks-Balconi\">"
        "<h1 class=\"card-name\">William Weeks-Balconi</h1>"
        "<p class=\"card-bio\">Husband, father, Christian, historian, builder.<br>"
        "Into Clojure, Microsoft Fabric, and writing about Infrastructure as Code.<br>"
        "I prefer to be contacted by email.</p>"
        "<a class=\"add-contact-btn\" href=\"/static/contact.vcf\" download=\"william-weeks-balconi.vcf\">"
        "<span class=\"link-icon\">👤</span>"
        "<span class=\"link-label\">Add to Contacts</span>"
        "</a>"
        "<nav class=\"links\" aria-label=\"Contact links\">"
        (apply str (map link-item contact-links))
        "</nav>"
        "</div>")
   :card-layout true))

(defn- resume-body
  "Resume content as HTML — shared between the web page and the Word download."
  []
  (str
    "<section class=\"resume\">"
    "<header class=\"resume-header\">"
    "<h1>William Weeks-Balconi</h1>"
    "<p class=\"resume-title\">Senior Cloud Systems Administrator — Azure, Windows Server &amp; Network Infrastructure</p>"
    "<p class=\"resume-contact\">"
    "<a href=\"mailto:jump@hey.com\">jump@hey.com</a> &nbsp;·&nbsp; "
    "<a href=\"https://linkedin.com/in/acestus\">linkedin.com/in/acestus</a>"
    "</p>"
    "</header>"

    "<div class=\"resume-section\">"
    "<h2>Profile</h2>"
    "<p>Senior cloud systems administrator with 8+ years designing, deploying, and operating secure Microsoft Azure environments alongside Windows Server and hybrid network infrastructure. I run production workloads on Azure VMs, VNETs, NSGs, and load balancers; enforce least-privilege access with Entra ID, RBAC, PIM, and Azure Policy; and keep systems healthy with Azure Monitor, Log Analytics, and Application Insights. I write Infrastructure as Code (Bicep / ARM / Terraform) deployed through OIDC-federated CI/CD, and I treat backup, disaster recovery, and documentation as first-class deliverables. Comfortable translating technical detail for non-technical stakeholders and partnering across developer, security, and network teams.</p>"
    "</div>"

    "<div class=\"resume-section\">"
    "<h2>Skills</h2>"
    "<ul class=\"resume-skills\">"
    "<li><strong>Azure Infrastructure</strong> — Virtual Machines, VNETs, NSGs, Load Balancers, Storage Accounts, Resource Groups, Azure Firewall, Front Door, AKS</li>"
    "<li><strong>Networking</strong> — TCP/IP, DNS, VPN (Site-to-Site &amp; Point-to-Site), ExpressRoute, on-prem LAN/WAN, firewall policy, network security protocols</li>"
    "<li><strong>Windows Server &amp; Identity</strong> — Active Directory, Entra ID, Entra Connect Sync, Group Policy, MFA, Conditional Access, PIM, RBAC</li>"
    "<li><strong>Monitoring &amp; Performance</strong> — Azure Monitor, Log Analytics, Application Insights, KQL, Resource Graph, Microsoft Sentinel, alerting, cost optimization</li>"
    "<li><strong>Security &amp; Compliance</strong> — Azure Policy, NSGs, vulnerability scanning, SIEM, FedRAMP, backup &amp; disaster recovery, Azure Site Recovery</li>"
    "<li><strong>Automation &amp; IaC</strong> — Bicep (AVM), ARM, Terraform, PowerShell, Logic Apps, Azure Automation runbooks, GitHub Actions with OIDC</li>"
    "<li><strong>Data &amp; Database</strong> — Microsoft Fabric, OneLake, Synapse, Azure SQL, relational database administration</li>"
    "</ul>"
    "</div>"

    "<div class=\"resume-section\">"
    "<h2>Certifications</h2>"
    "<ul class=\"resume-certs\">"
    "<li>AZ-500 · AZ-305 · AZ-104 · DP-900</li>"
    "<li>CKA · CCNA · CompTIA Server+</li>"
    "</ul>"
    "</div>"

    "<div class=\"resume-section\">"
    "<h2>Experience</h2>"

    "<div class=\"resume-job\">"
    "<div class=\"resume-job-header\">"
    "<span class=\"resume-company\">Reprise Financial</span>"
    "<span class=\"resume-dates\">Jun 2024 – Present</span>"
    "</div>"
    "<div class=\"resume-role\">Cloud Infrastructure Engineer</div>"
    "<ul>"
    "<li>Operate Azure infrastructure — VMs, VNETs, storage accounts, load balancers, and resource groups — with high availability and FedRAMP compliance</li>"
    "<li>Author Bicep (AVM) and Terraform Infrastructure as Code deployed through GitHub Actions with OIDC federated identity (no long-lived secrets)</li>"
    "<li>Build Azure Monitor, Log Analytics, and Application Insights dashboards; write KQL queries to track system health, performance, and cost</li>"
    "<li>Manage Azure backup, snapshot, and disaster recovery configurations; test restore procedures to validate business continuity</li>"
    "<li>Enforce security baselines via Azure Policy, NSGs, RBAC, and Microsoft Sentinel SIEM analytics rules</li>"
    "<li>Optimize cloud spend with right-sizing, reserved instances, and FinOps reporting across Azure, Fabric, and AI Foundry</li>"
    "<li>Support Kubernetes (AKS) workloads and produce technical documentation and runbooks for the operations team</li>"
    "</ul>"
    "</div>"

    "<div class=\"resume-job\">"
    "<div class=\"resume-job-header\">"
    "<span class=\"resume-company\">Beretta Holdings</span>"
    "<span class=\"resume-dates\">Dec 2023 – Apr 2024</span>"
    "</div>"
    "<div class=\"resume-role\">Senior Azure Architect</div>"
    "<ul>"
    "<li>Subject matter expert for Azure, Microsoft 365, and Microsoft Sentinel across a multi-region enterprise tenant</li>"
    "<li>Migrated on-premises Windows Server workloads to Azure VMs and AKS, including Active Directory integration with Entra ID</li>"
    "<li>Designed and deployed VNETs, subnets, Site-to-Site VPN, and Azure Firewall policy for secure hybrid connectivity</li>"
    "<li>Provisioned cloud infrastructure with ARM templates and Bicep; codified network security groups and Azure Policy assignments</li>"
    "<li>Implemented Entra ID hardening — MFA, Conditional Access, Privileged Identity Management (PIM), and role-based access control</li>"
    "<li>Configured Azure Backup and Azure Site Recovery; documented and tested disaster recovery runbooks</li>"
    "</ul>"
    "</div>"

    "<div class=\"resume-job\">"
    "<div class=\"resume-job-header\">"
    "<span class=\"resume-company\">Microsoft</span>"
    "<span class=\"resume-dates\">Jul 2022 – Dec 2023</span>"
    "</div>"
    "<div class=\"resume-role\">Cloud Engineer</div>"
    "<ul>"
    "<li>Supported Azure customers across compute, networking, identity, and observability — frequently translating technical detail for non-technical stakeholders</li>"
    "<li>Authored PowerShell runbooks for Azure Automation and Logic Apps to remediate alerts and standardize operations</li>"
    "<li>Deployed Azure Front Door profiles for CDN, global load balancing, and high availability</li>"
    "<li>Wrote Kusto (KQL) queries against Log Analytics, Azure Resource Graph, and Sentinel for performance trending and security investigation</li>"
    "<li>Deployed and tuned Azure Monitor Agent (AMA/MMA) with Data Collection Rules to standardize telemetry across Windows and Linux fleets</li>"
    "<li>Reviewed NSG, Azure Policy, and Conditional Access configurations during customer engagements</li>"
    "</ul>"
    "</div>"

    "<div class=\"resume-job\">"
    "<div class=\"resume-job-header\">"
    "<span class=\"resume-company\">Stock Technologies</span>"
    "<span class=\"resume-dates\">Mar 2020 – May 2022</span>"
    "</div>"
    "<div class=\"resume-role\">Cloud Operations Engineer</div>"
    "<ul>"
    "<li>Provided IT consulting and Tier 2/3 systems administration for PathAdvantage, a multi-site medical practice</li>"
    "<li>Maintained Entra Connect (Azure AD Connect) hybrid identity sync between on-prem Active Directory and Microsoft 365</li>"
    "<li>Migrated regulated healthcare data and Windows Server workloads to Azure with backup and recovery validation</li>"
    "<li>Automated user on-boarding, off-boarding, and license assignment with PowerShell and Microsoft Graph</li>"
    "</ul>"
    "</div>"

    "<div class=\"resume-job\">"
    "<div class=\"resume-job-header\">"
    "<span class=\"resume-company\">Beacon Hill Staffing</span>"
    "<span class=\"resume-dates\">Apr 2017 – Mar 2020</span>"
    "</div>"
    "<div class=\"resume-role\">Cloud Administrator</div>"
    "<ul>"
    "<li>Cloud and Windows Server administration consulting for 7-Eleven and CBRE enterprise environments</li>"
    "<li>Managed Azure IaaS — VMs, NSGs, virtual networks, and storage — with regular vulnerability scanning and remediation</li>"
    "<li>Administered Active Directory, Group Policy, and on-prem network connectivity to Azure</li>"
    "<li>Wrote SQL and PowerShell automation for daily operations, patching reports, and security compliance</li>"
    "</ul>"
    "</div>"

    "</div>"

    "<div class=\"resume-section\">"
    "<h2>Education</h2>"
    "<p><strong>Austin College — Sherman, Texas</strong><br>"
    "BS Computer Science &amp; BA History, 2009</p>"
    "</div>"

    "</section>"))

(defn resume-page []
  (page-shell
   "Resume"
   (str
    "<div class=\"resume-actions\">"
    "<button class=\"resume-print\" onclick=\"window.print()\" aria-label=\"Download resume as PDF\">"
    "<span aria-hidden=\"true\">⬇</span> Download PDF"
    "</button>"
    "<a class=\"resume-print\" href=\"/resume.doc\" download=\"william-weeks-balconi-resume.doc\" aria-label=\"Download resume as Word document\">"
    "<span aria-hidden=\"true\">⬇</span> Download Word"
    "</a>"
    "</div>"
    (resume-body))))

(def ^:private resume-doc-style
  "Inline styles for the Word download. External CSS isn't followed by Word, so
   we keep a minimal, print-shaped block right in the HTML."
  (str
   "body { font-family: Calibri, Arial, sans-serif; font-size: 11pt; color: #000; max-width: 7.5in; margin: 0.5in auto; }"
   "h1 { font-size: 18pt; margin: 0 0 0.1in 0; }"
   "h2 { font-size: 11pt; text-transform: uppercase; letter-spacing: 0.05em; color: #333; border-bottom: 1px solid #999; padding-bottom: 2pt; margin: 0.2in 0 0.1in 0; }"
   ".resume-title { font-size: 12pt; color: #333; margin: 0 0 0.05in 0; }"
   ".resume-contact { font-size: 10pt; color: #333; margin: 0 0 0.2in 0; }"
   ".resume-contact a { color: #000; text-decoration: none; }"
   ".resume-section { margin-bottom: 0.15in; }"
   ".resume-job { margin-bottom: 0.15in; }"
   ".resume-job-header { display: block; }"
   ".resume-company { font-weight: bold; font-size: 11pt; }"
   ".resume-dates { float: right; font-size: 10pt; color: #333; }"
   ".resume-role { font-style: italic; font-size: 10.5pt; color: #333; margin-bottom: 0.05in; }"
   "ul { margin: 0.05in 0; padding-left: 0.25in; }"
   "li { margin-bottom: 0.03in; font-size: 10.5pt; }"
   "strong { font-weight: bold; }"))

(defn resume-doc
  "Self-contained HTML document Word opens as a .doc file."
  []
  (str "<!DOCTYPE html><html lang=\"en\"><head>"
       "<meta charset=\"utf-8\">"
       "<title>William Weeks-Balconi — Resume</title>"
       "<style>" resume-doc-style "</style>"
       "</head><body>"
       (resume-body)
       "</body></html>"))

(defn not-found-page []
  (page-shell "Not Found" "<h1>404</h1><p>Nothing here.</p>"))
