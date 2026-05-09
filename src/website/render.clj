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
        "<p class=\"card-bio\">Husband, father, Christian, historian.<br>"
        "Into Clojure, Microsoft Fabric, writing about Infrastructure as Code, building OpenClaw.</p>"
        "<nav class=\"links\" aria-label=\"Site links\">"
        "<a class=\"link-btn\" href=\"/blog\"><span class=\"link-icon\">✍</span><span class=\"link-label\">Blog</span></a>"
        "<a class=\"link-btn\" href=\"/contact\"><span class=\"link-icon\">✉</span><span class=\"link-label\">Contact</span></a>"
        "<a class=\"link-btn\" href=\"https://github.com/Acestus/website\" rel=\"noopener noreferrer\"><span class=\"link-icon\">⌨</span><span class=\"link-label\">Source Code</span></a>"
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
  [{:label "Email"    :url "mailto:jump@hey.com"                        :icon "✉"}
   {:label "Blog"     :url "/blog"                                      :icon "✍"}
   {:label "Website"  :url "https://www.acestus.com"                   :icon "🌐"}
   {:label "GitHub"   :url "https://github.com/Acestus"                :icon "⌨"}
   {:label "Mastodon" :url "https://social.linux.pizza/@acestus"        :icon "🐘" :rel "me"}
   {:label "Telegram" :url "https://t.me/acestus"                       :icon "✈"}
   {:label "LinkedIn" :url "https://linkedin.com/in/acestus"           :icon "💼"}])

(defn- link-item [{:keys [label url icon rel]}]
  (let [rel-val (cond
                  rel                          (str "noopener noreferrer " rel)
                  (str/starts-with? url "http") "noopener noreferrer"
                  :else                        nil)]
    (str "<a class=\"link-btn\" href=\"" url "\""
         (when rel-val (str " rel=\"" rel-val "\""))
         "><span class=\"link-icon\">" icon "</span>"
         "<span class=\"link-label\">" label "</span></a>")))

(defn contact-page []
  (page-shell
   "Contact"
   (str "<div class=\"card\">"
        "<img class=\"avatar-photo\" src=\"/static/photo.png\" alt=\"William Weeks-Balconi\">"
        "<h1 class=\"card-name\">William Weeks-Balconi</h1>"
        "<p class=\"card-bio\">Husband, father, Christian, historian.<br>"
        "Into Clojure, Microsoft Fabric, writing about Infrastructure as Code, building OpenClaw.</p>"
        "<a class=\"add-contact-btn\" href=\"/static/contact.vcf\" download=\"william-weeks-balconi.vcf\">"
        "<span class=\"link-icon\">👤</span>"
        "<span class=\"link-label\">Add to Contacts</span>"
        "</a>"
        "<nav class=\"links\" aria-label=\"Contact links\">"
        (apply str (map link-item contact-links))
        "</nav>"
        "</div>")
   :card-layout true))

(defn resume-page []
  (page-shell
   "Resume"
   (str
    "<section class=\"resume\">"
    "<header class=\"resume-header\">"
    "<h1>William Weeks-Balconi</h1>"
    "<p class=\"resume-title\">Platform Engineer — Fabric, Azure &amp; Clojure</p>"
    "<p class=\"resume-contact\">"
    "<a href=\"mailto:jump@hey.com\">jump@hey.com</a> &nbsp;·&nbsp; "
    "<a href=\"https://linkedin.com/in/acestus\">linkedin.com/in/acestus</a>"
    "</p>"
    "</header>"

    "<div class=\"resume-section\">"
    "<h2>Profile</h2>"
    "<p>I manage Azure environments with a Linux tech stack. Platform Engineer with 5+ years experience in Microsoft Fabric, Azure infrastructure, IT automation, and cloud services. Specializing in functional programming with Clojure and Infrastructure as Code.</p>"
    "</div>"

    "<div class=\"resume-section\">"
    "<h2>Skills</h2>"
    "<ul class=\"resume-skills\">"
    "<li><strong>Data Engineering</strong> — Microsoft Fabric, Lakehouse, OneLake, Data Factory, Synapse pipelines</li>"
    "<li><strong>Cloud Native Deployment</strong> — Kubernetes for Azure and Google Cloud</li>"
    "<li><strong>Network Administration</strong> — Routers and firewalls with Infrastructure as Code</li>"
    "<li><strong>Security &amp; Compliance</strong> — Backup, disaster recovery, RBAC, SIEM, Entra, FedRAMP</li>"
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
    "<li>Optimize billing and cost management for cloud services</li>"
    "<li>Build analytic relational databases</li>"
    "<li>Write Infrastructure-as-Code with CI/CD pipelines</li>"
    "<li>Support Kubernetes and cloud-native apps</li>"
    "<li>Maintain FedRAMP compliance with SIEM</li>"
    "<li>Write automation scripts with technical documentation</li>"
    "</ul>"
    "</div>"

    "<div class=\"resume-job\">"
    "<div class=\"resume-job-header\">"
    "<span class=\"resume-company\">Beretta Holdings</span>"
    "<span class=\"resume-dates\">Dec 2023 – Apr 2024</span>"
    "</div>"
    "<div class=\"resume-role\">Senior Azure Architect</div>"
    "<ul>"
    "<li>SME for Azure, Microsoft 365, and Microsoft Sentinel</li>"
    "<li>Deployed AKS and migrated on-prem servers to Azure</li>"
    "<li>Provisioned resources using ARM templates and Bicep</li>"
    "<li>Designed subnets, VNETs, VPN, and Azure Firewall policy</li>"
    "<li>Implemented Entra ID with MFA, PIM, and RBAC</li>"
    "</ul>"
    "</div>"

    "<div class=\"resume-job\">"
    "<div class=\"resume-job-header\">"
    "<span class=\"resume-company\">Microsoft</span>"
    "<span class=\"resume-dates\">Jul 2022 – Dec 2023</span>"
    "</div>"
    "<div class=\"resume-role\">Cloud Engineer</div>"
    "<ul>"
    "<li>Supported Azure Kubernetes Service (AKS)</li>"
    "<li>Created custom runbooks with PowerShell for Logic Apps and Azure Automation</li>"
    "<li>Deployed Azure Front Door profiles for CDN and HA</li>"
    "<li>Wrote Kusto queries for Log Analytics, Resource Graph, and Sentinel</li>"
    "<li>Deployed and managed AMA/MMA agents with Data Collection Rules</li>"
    "</ul>"
    "</div>"

    "<div class=\"resume-job\">"
    "<div class=\"resume-job-header\">"
    "<span class=\"resume-company\">Stock Technologies</span>"
    "<span class=\"resume-dates\">Mar 2020 – May 2022</span>"
    "</div>"
    "<div class=\"resume-role\">Cloud Operations Engineer</div>"
    "<ul>"
    "<li>IT consulting for PathAdvantage medical facility</li>"
    "<li>Maintained Azure AD Connect Sync</li>"
    "<li>Migrated healthcare data to Azure</li>"
    "<li>Wrote PowerShell scripts for user on-boarding automation</li>"
    "</ul>"
    "</div>"

    "<div class=\"resume-job\">"
    "<div class=\"resume-job-header\">"
    "<span class=\"resume-company\">Beacon Hill Staffing</span>"
    "<span class=\"resume-dates\">Apr 2017 – Mar 2020</span>"
    "</div>"
    "<div class=\"resume-role\">Cloud Administrator</div>"
    "<ul>"
    "<li>Consulting for 7-Eleven and CBRE</li>"
    "<li>Managed IAAS, Azure VMs, NSGs, and vulnerability scanning</li>"
    "<li>Wrote SQL and PowerShell scripts for operations and security</li>"
    "</ul>"
    "</div>"

    "</div>"

    "<div class=\"resume-section\">"
    "<h2>Education</h2>"
    "<p><strong>Austin College — Sherman, Texas</strong><br>"
    "BS Computer Science &amp; BA History, 2009</p>"
    "</div>"

    "</section>")))

(defn not-found-page []
  (page-shell "Not Found" "<h1>404</h1><p>Nothing here.</p>"))
