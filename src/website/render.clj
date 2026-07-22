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

(def ^:private resume-contact-html
  (str "<p class=\"resume-contact\">"
       "<a href=\"mailto:jump@hey.com\">jump@hey.com</a> &nbsp;·&nbsp; "
       "<a href=\"https://linkedin.com/in/acestus\">linkedin.com/in/acestus</a>"
       "</p>"))

(def ^:private resume-experience
  [{:company "Reprise Financial"
    :dates "Jun 2024 – Present"
    :role "Cloud Infrastructure Engineer"
    :bullets ["Optimize cloud billing, capacity, and cost management for production services"
              "Build analytic relational databases and Microsoft Fabric data platform components"
              "Write Infrastructure-as-Code with CI/CD pipelines for repeatable environments"
              "Support Kubernetes and cloud-native applications across Linux-based operations"
              "Maintain FedRAMP-aligned controls with SIEM monitoring and technical documentation"
              "Use AI coding assistants and local automation to accelerate troubleshooting, documentation, and platform buildout"]}
   {:company "Beretta Holdings"
    :dates "Dec 2023 – Apr 2024"
    :role "Senior Azure Architect"
    :bullets ["Served as SME for Azure, Microsoft 365, and Microsoft Sentinel"
              "Deployed AKS and migrated on-premises servers to Azure"
              "Provisioned resources using ARM templates and Bicep Infrastructure as Code"
              "Designed subnets, VNETs, VPN, and Azure Firewall policy"
              "Implemented Entra ID with MFA, PIM, and RBAC for governed access"]}
   {:company "Microsoft"
    :dates "Jul 2022 – Dec 2023"
    :role "Cloud Engineer"
    :bullets ["Supported Azure Kubernetes Service production issues and customer escalations"
              "Created custom runbooks with PowerShell for Logic Apps and Azure Automation"
              "Deployed Azure Front Door profiles for CDN, high availability, and traffic routing"
              "Wrote Kusto queries for Log Analytics, Resource Graph, and Microsoft Sentinel"
              "Deployed and managed AMA/MMA agents with Data Collection Rules"]}
   {:company "Stock Technologies"
    :dates "Mar 2020 – May 2022"
    :role "Cloud Operations Engineer"
    :bullets ["Delivered IT consulting for PathAdvantage medical facility"
              "Maintained Azure AD Connect Sync and hybrid identity workflows"
              "Migrated healthcare data to Azure with operational safeguards"
              "Wrote PowerShell scripts for user onboarding automation"]}
   {:company "Beacon Hill Staffing"
    :dates "Apr 2017 – Mar 2020"
    :role "Cloud Administrator"
    :bullets ["Consulted for 7-Eleven and CBRE"
              "Managed IaaS, Azure VMs, NSGs, and vulnerability scanning"
              "Wrote SQL and PowerShell scripts for operations and security"]}])

(def ^:private resume-variants
  {:platform
   {:title "Resume"
    :headline "Platform Engineer — Fabric, Azure &amp; Clojure"
    :profile "I manage Azure environments with a Linux tech stack. Platform Engineer with 5+ years experience in Microsoft Fabric, Azure infrastructure, IT automation, and cloud services. Specializing in functional programming with Clojure and Infrastructure as Code."
    :skills [["Data Engineering" "Microsoft Fabric, Lakehouse, OneLake, Data Factory, Synapse pipelines"]
             ["Cloud Native Deployment" "Kubernetes for Azure and Google Cloud"]
             ["Network Administration" "Routers and firewalls with Infrastructure as Code"]
             ["Security &amp; Compliance" "Backup, disaster recovery, RBAC, SIEM, Entra, FedRAMP"]]
    :certs ["AZ-500 · AZ-305 · AZ-104 · DP-900"
            "CKA · CCNA · CompTIA Server+"]
    :experience resume-experience}

   :ai-platform
   {:title "AI Platform Engineer Resume"
    :headline "Senior AI Platform Engineer — Responsible Agentic Systems, Cloud Infrastructure &amp; Automation"
    :profile "Senior platform engineer focused on turning agentic AI into production infrastructure: secure agent runtimes, reusable deployment patterns, CI/CD standardization, governed identity and secrets access, observability, rollback, runbooks, and cost-aware cloud operations. Hands-on with Azure, Kubernetes, GitHub Actions, Infrastructure as Code, Microsoft Fabric, M365 Copilot governance, local/open-source LLM operations, and AI-assisted engineering workflows using Codex, Claude Code, Cursor, Copilot, and OpenClaw-style skills."
    :skills [["AI Platform Engineering" "Responsible agentic runtime patterns, tool calling, RAG workflows, evaluation loops, guardrails, human approval gates, production readiness standards"]
             ["AI-Assisted Delivery" "Codex, Claude Code, Cursor, GitHub Copilot, reusable skills, ticket-to-code workflows, automated documentation, troubleshooting, and test generation"]
             ["Cloud &amp; IaC" "Azure, hybrid cloud, Docker, Kubernetes, AKS, Bicep, Terraform-ready patterns, Helm-style release standardization, GitHub Actions reusable workflows"]
             ["Security &amp; Identity" "OAuth 2.0, OIDC, SAML, JWT, RBAC, IAM, Entra ID, PIM, workload identity, secrets management, service-to-service access"]
             ["Operations &amp; Observability" "Incident response, Kusto, Log Analytics, Microsoft Sentinel, SIEM, traces/logs/metrics, rollback, DR, capacity planning, cost optimization"]
             ["Languages &amp; Automation" "Clojure, ClojureScript, Python, JavaScript, PowerShell, Bash, SQL, Linux, containers, networking"]]
    :certs ["AZ-500 · AZ-305 · AZ-104 · DP-900"
            "CKA · CCNA · CompTIA Server+"]
    :experience
    [(update (nth resume-experience 0) :bullets
             (constantly ["Automate cloud platform buildout, release paths, and environment provisioning with Infrastructure as Code and CI/CD"
                          "Use AI coding tools and OpenClaw/Codex-style skills to automate ticket investigation, documentation, deployment support, and troubleshooting"
                          "Build Microsoft Fabric and analytic platform components with repeatable operational patterns"
                          "Support Kubernetes and cloud-native Linux services with monitoring, rollback, and incident response practices"
                          "Maintain FedRAMP-aligned security posture through SIEM, access control, technical documentation, and audit-ready operations"
                          "Drive cost, capacity, and reliability tradeoffs for cloud services and data workloads"]))]
    :projects
    [{:name "OpenClaw and Codex Skills"
      :details "Built and operates a local AI-operations environment where agents use skills, tools, shell commands, GitHub, Reminders, Notion, and messaging to complete real work with human approval boundaries and durable memory."}
     {:name "Workflow Toolkit"
      :details "Open-source file-driven AI ops pattern with reusable skills, markdown-to-API synchronization, ticket workflows, GitHub Actions, and local SQLite state for auditable handoffs."}
     {:name "Copilot-Ops Console"
      :details "Portfolio implementation showing agentic ticket lanes, skill pipelines, voice capture, worklog sync, and operator-in-the-loop automation for production support."}
     {:name "Agentic Web Portfolio"
      :details "Explains the production architecture model for API-first agents using REST/GraphQL/CLI, OAuth/OIDC, scoped permissions, and composable tool access instead of brittle custom middleware."}]}

   :sre
   {:title "Site Reliability Engineer Resume"
    :headline "Site Reliability Engineer — Cloud Operations, Kubernetes, Observability &amp; Incident Response"
    :profile "Cloud and site reliability engineer with production experience across Azure, Kubernetes, Linux operations, Infrastructure as Code, monitoring, security, disaster recovery, and cost optimization. Strong at incident response, Kusto investigations, CI/CD, runbooks, rollback planning, hybrid identity, and translating messy production constraints into reliable operating practices."
    :skills [["Reliability Engineering" "Incident response, monitoring, rollback, disaster recovery, capacity planning, operational readiness, runbooks"]
             ["Cloud Platforms" "Azure, AKS, Azure Front Door, Azure Automation, Logic Apps, Microsoft Fabric, hybrid cloud operations"]
             ["Observability &amp; Security" "Log Analytics, Kusto/KQL, Microsoft Sentinel, SIEM, vulnerability scanning, AMA/MMA agents, Data Collection Rules"]
             ["Infrastructure &amp; Delivery" "Bicep, ARM templates, CI/CD, GitHub Actions, Kubernetes, Docker, Linux, PowerShell, Bash, SQL"]
             ["Identity &amp; Networking" "Entra ID, MFA, PIM, RBAC, Azure AD Connect, VNETs, subnets, VPN, NSGs, Azure Firewall"]]
    :certs ["AZ-500 · AZ-305 · AZ-104 · DP-900"
            "CKA · CCNA · CompTIA Server+"]
    :experience resume-experience
    :projects
    [{:name "Infrastructure as Code Portfolio"
      :details "Interactive portfolio and articles showing Azure deployment pipelines, Bicep/AVM patterns, GitHub Actions, OIDC, policy governance, and repeatable cloud delivery."}
     {:name "Log and Operations Portfolio"
      :details "Operational demos covering KQL, log pipelines, Azure monitoring, incident investigation, and production support practices."}
     {:name "Fabric Capacity and FinOps Work"
      :details "Cost and capacity optimization examples for Microsoft Fabric and Azure, including SKU selection, pause/resume strategy, and operational guardrails."}]}})

(defn- html-list [class-name items]
  (str "<ul" (when-not (str/blank? class-name) (str " class=\"" class-name "\"")) ">"
       (str/join (for [item items] (str "<li>" item "</li>")))
       "</ul>"))

(defn- skill-item [[category details]]
  (str "<strong>" category "</strong> — " details))

(defn- job-html [{:keys [company dates role bullets]}]
  (str "<div class=\"resume-job\">"
       "<div class=\"resume-job-header\">"
       "<span class=\"resume-company\">" company "</span>"
       "<span class=\"resume-dates\">" dates "</span>"
       "</div>"
       "<div class=\"resume-role\">" role "</div>"
       (html-list "" bullets)
       "</div>"))

(defn- project-html [{:keys [name details]}]
  (str "<div class=\"resume-job\">"
       "<div class=\"resume-role\"><strong>" name "</strong></div>"
       "<p>" details "</p>"
       "</div>"))

(defn resume-doc-path [variant]
  (case variant
    :ai-platform "/resume/ai-platform-engineer.doc"
    :sre "/resume/site-reliability-engineer.doc"
    "/resume.doc"))

(defn resume-download-filename [variant]
  (case variant
    :ai-platform "william-weeks-balconi-ai-platform-engineer-resume.doc"
    :sre "william-weeks-balconi-site-reliability-engineer-resume.doc"
    "william-weeks-balconi-resume.doc"))

(defn- resume-actions [variant]
  (str "<div class=\"resume-actions\">"
       "<button class=\"resume-print\" onclick=\"window.print()\" aria-label=\"Download resume as PDF\">"
       "<span aria-hidden=\"true\">⬇</span> Download PDF"
       "</button>"
       "<a class=\"resume-print\" href=\"" (resume-doc-path variant) "\" download=\"" (resume-download-filename variant) "\" aria-label=\"Download resume as Word document\">"
       "<span aria-hidden=\"true\">⬇</span> Download Word"
       "</a>"
       "</div>"))

(defn- resume-body
  "Resume content as HTML, shared by the web page and Word download."
  [variant]
  (let [{:keys [headline profile skills certs experience projects]}
        (get resume-variants variant (:platform resume-variants))]
    (str "<section class=\"resume\">"
       "<header class=\"resume-header\">"
       "<h1>William Weeks-Balconi</h1>"
       "<p class=\"resume-title\">" headline "</p>"
       resume-contact-html
       "</header>"

       "<div class=\"resume-section\">"
       "<h2>Profile</h2>"
       "<p>" profile "</p>"
       "</div>"

       "<div class=\"resume-section\">"
       "<h2>Skills</h2>"
       (html-list "resume-skills" (map skill-item skills))
       "</div>"

       "<div class=\"resume-section\">"
       "<h2>Certifications</h2>"
       (html-list "resume-certs" certs)
       "</div>"

       "<div class=\"resume-section\">"
       "<h2>Experience</h2>"
       (apply str (map job-html experience))
       "</div>"

       (when (seq projects)
         (str "<div class=\"resume-section\">"
              "<h2>Selected Projects</h2>"
              (apply str (map project-html projects))
              "</div>"))

       "<div class=\"resume-section\">"
       "<h2>Education</h2>"
       "<p><strong>Austin College — Sherman, Texas</strong><br>"
       "BS Computer Science &amp; BA History, 2009</p>"
       "</div>"

       "</section>")))

(defn resume-page
  ([] (resume-page :platform))
  ([variant]
   (let [{:keys [title]} (get resume-variants variant (:platform resume-variants))]
     (page-shell
      title
      (str
       (resume-actions variant)
       (resume-body variant))))))

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
  ([] (resume-doc :platform))
  ([variant]
   (let [{:keys [title]} (get resume-variants variant (:platform resume-variants))]
     (str "<!DOCTYPE html><html lang=\"en\"><head>"
          "<meta charset=\"utf-8\">"
          "<title>William Weeks-Balconi — " title "</title>"
          "<style>" resume-doc-style "</style>"
          "</head><body>"
          (resume-body variant)
          "</body></html>"))))

(defn not-found-page []
  (page-shell "Not Found" "<h1>404</h1><p>Nothing here.</p>"))
