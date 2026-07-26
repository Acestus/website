export type PostMeta = {
  slug: string;
  title: string;
  date: string;
  summary: string;
};

export type ResumeVariantKey = "platform" | "ai-platform" | "sre";

export type ResumeVariant = {
  title: string;
  headline: string;
  profile: string;
  skills: Array<[string, string]>;
  certs: string[];
  experience: Array<{
    company: string;
    dates: string;
    role: string;
    bullets: string[];
  }>;
  projects?: Array<{
    name: string;
    details: string;
  }>;
};

export const posts: PostMeta[] = [
  {
    slug: "i-m-a-historian-by-training-the-first-thing-you-learn-is-the-hierarchy-of-sources-primary-sources-8d25c327",
    title:
      "I'm a historian by training. The first thing you learn is the hierarchy of sources. Primary sources are the raw material.",
    date: "2026-05-30T00:23:44Z",
    summary:
      "I'm a historian by training. The first thing you learn is the hierarchy of sources. Primary sources are the raw material. Secondary sources are someone else's interpretation. The further you get from the primary, the more distortion accumulates...",
  },
  {
    slug: "purpose-built-ai-a-field-toolkit-for-engineering-operations-f069fef1",
    title: "Purpose-Built AI: A Field Toolkit for Engineering Operations",
    date: "2026-05-29T23:56:33Z",
    summary:
      "General-purpose AI assistants have an infinite goal space. Give one the instruction \"answer whatever the user asks\" and you've created a system with no defensible perimeter...",
  },
  {
    slug: "why-modern-movies-with-iphones-leave-me-cold-and-what-star-trek-gets-right-about-technology-6167feb0",
    title: "Why Modern Movies with iPhones Leave Me Cold (And What Star Trek Gets Right About Technology)",
    date: "2026-05-03T03:50:57Z",
    summary:
      "Lately, I’ve been bothered by how so many movies treat the iPhone as the be-all, end-all device—every key moment ends up being people yelling into a little slab of glass...",
  },
  {
    slug: "great-writers-freeze-a-language-2e0af7fc",
    title: "Great Writers Freeze a Language",
    date: "2026-05-02T05:00:13Z",
    summary:
      "Looking back at history, I’m often reminded of the famous Mark Twain quote: “History doesn’t repeat itself, but it does rhyme.”...",
  },
  {
    slug: "why-i-love-trunk-based-development-9e7b07c2",
    title: "Why I Love Trunk-Based Development",
    date: "2026-05-02T04:48:37Z",
    summary:
      "For a long time, I used to think branching strategies were just a technical detail. The more I build and work with teams, the more I’ve come to appreciate trunk-based development...",
  },
  {
    slug: "the-power-of-our-words-1112cb8e",
    title: "The Power of Our Words",
    date: "2026-05-02T04:11:10Z",
    summary:
      "Recently, I listened to a talk at my church that really stuck with me. A few things hit home—simple ideas on how to show love...",
  },
];

export const postsByDate = [...posts].sort((a, b) => b.date.localeCompare(a.date));

export const contactLinks = [
  { label: "Email", detail: "jump@hey.com", url: "mailto:jump@hey.com", icon: "✉" },
  { label: "Blog", detail: "acestus.com/blog", url: "/blog", icon: "✍" },
  { label: "Website", detail: "acestus.com", url: "https://www.acestus.com", icon: "🌐" },
  { label: "GitHub", detail: "github.com/Acestus", url: "https://github.com/Acestus", icon: "⌨" },
  {
    label: "Mastodon",
    detail: "@acestus@social.linux.pizza",
    url: "https://social.linux.pizza/@acestus",
    icon: "🐘",
    rel: "me",
  },
  { label: "Telegram", detail: "t.me/acestus", url: "https://t.me/acestus", icon: "✈" },
  { label: "LinkedIn", detail: "linkedin.com/in/acestus", url: "https://linkedin.com/in/acestus", icon: "💼" },
];

const resumeExperience = [
  {
    company: "Reprise Financial",
    dates: "Jun 2024 – Present",
    role: "Cloud Infrastructure Engineer",
    bullets: [
      "Optimize cloud billing, capacity, and cost management for production services",
      "Build analytic relational databases and Microsoft Fabric data platform components",
      "Write Infrastructure-as-Code with CI/CD pipelines for repeatable environments",
      "Support Kubernetes and cloud-native applications across Linux-based operations",
      "Maintain FedRAMP-aligned controls with SIEM monitoring and technical documentation",
      "Use AI coding assistants and local automation to accelerate troubleshooting, documentation, and platform buildout",
    ],
  },
  {
    company: "Beretta Holdings",
    dates: "Dec 2023 – Apr 2024",
    role: "Senior Azure Architect",
    bullets: [
      "Served as SME for Azure, Microsoft 365, and Microsoft Sentinel",
      "Deployed AKS and migrated on-premises servers to Azure",
      "Provisioned resources using ARM templates and Bicep Infrastructure as Code",
      "Designed subnets, VNETs, VPN, and Azure Firewall policy",
      "Implemented Entra ID with MFA, PIM, and RBAC for governed access",
    ],
  },
  {
    company: "Microsoft",
    dates: "Jul 2022 – Dec 2023",
    role: "Cloud Engineer",
    bullets: [
      "Supported Azure Kubernetes Service production issues and customer escalations",
      "Created custom runbooks with PowerShell for Logic Apps and Azure Automation",
      "Deployed Azure Front Door profiles for CDN, high availability, and traffic routing",
      "Wrote Kusto queries for Log Analytics, Resource Graph, and Microsoft Sentinel",
      "Deployed and managed AMA/MMA agents with Data Collection Rules",
    ],
  },
  {
    company: "Stock Technologies",
    dates: "Mar 2020 – May 2022",
    role: "Cloud Operations Engineer",
    bullets: [
      "Delivered IT consulting for PathAdvantage medical facility",
      "Maintained Azure AD Connect Sync and hybrid identity workflows",
      "Migrated healthcare data to Azure with operational safeguards",
      "Wrote PowerShell scripts for user onboarding automation",
    ],
  },
  {
    company: "Beacon Hill Staffing",
    dates: "Apr 2017 – Mar 2020",
    role: "Cloud Administrator",
    bullets: [
      "Consulted for 7-Eleven and CBRE",
      "Managed IaaS, Azure VMs, NSGs, and vulnerability scanning",
      "Wrote SQL and PowerShell scripts for operations and security",
    ],
  },
];

export const resumeVariants: Record<ResumeVariantKey, ResumeVariant> = {
  platform: {
    title: "Resume",
    headline: "Platform Engineer — Fabric, Azure & Clojure",
    profile:
      "I manage Azure environments with a Linux tech stack. Platform Engineer with 5+ years experience in Microsoft Fabric, Azure infrastructure, IT automation, and cloud services. Specializing in functional programming with Clojure and Infrastructure as Code.",
    skills: [
      ["Data Engineering", "Microsoft Fabric, Lakehouse, OneLake, Data Factory, Synapse pipelines"],
      ["Cloud Native Deployment", "Kubernetes for Azure and Google Cloud"],
      ["Network Administration", "Routers and firewalls with Infrastructure as Code"],
      ["Security & Compliance", "Backup, disaster recovery, RBAC, SIEM, Entra, FedRAMP"],
    ],
    certs: ["AZ-500 · AZ-305 · AZ-104 · DP-900", "CKA · CCNA · CompTIA Server+"],
    experience: resumeExperience,
  },
  "ai-platform": {
    title: "AI Platform Engineer Resume",
    headline:
      "Senior AI Platform Engineer — Responsible Agentic Systems, Cloud Infrastructure & Automation",
    profile:
      "Senior platform engineer focused on turning agentic AI into production infrastructure: secure agent runtimes, reusable deployment patterns, CI/CD standardization, governed identity and secrets access, observability, rollback, runbooks, and cost-aware cloud operations. Hands-on with Azure, Kubernetes, GitHub Actions, Infrastructure as Code, Microsoft Fabric, M365 Copilot governance, local/open-source LLM operations, and AI-assisted engineering workflows using Codex, Claude Code, Cursor, Copilot, and OpenClaw-style skills.",
    skills: [
      ["AI Platform Engineering", "Responsible agentic runtime patterns, tool calling, RAG workflows, evaluation loops, guardrails, human approval gates, production readiness standards"],
      ["AI-Assisted Delivery", "Codex, Claude Code, Cursor, GitHub Copilot, reusable skills, ticket-to-code workflows, automated documentation, troubleshooting, and test generation"],
      ["Cloud & IaC", "Azure, hybrid cloud, Docker, Kubernetes, AKS, Bicep, Terraform-ready patterns, Helm-style release standardization, GitHub Actions reusable workflows"],
      ["Security & Identity", "OAuth 2.0, OIDC, SAML, JWT, RBAC, IAM, Entra ID, PIM, workload identity, secrets management, service-to-service access"],
      ["Operations & Observability", "Incident response, Kusto, Log Analytics, Microsoft Sentinel, SIEM, traces/logs/metrics, rollback, DR, capacity planning, cost optimization"],
      ["Languages & Automation", "Clojure, ClojureScript, Python, JavaScript, PowerShell, Bash, SQL, Linux, containers, networking"],
    ],
    certs: ["AZ-500 · AZ-305 · AZ-104 · DP-900", "CKA · CCNA · CompTIA Server+"],
    experience: resumeExperience,
    projects: [
      {
        name: "OpenClaw and Codex Skills",
        details:
          "Built and operates a local AI-operations environment where agents use skills, tools, shell commands, GitHub, Reminders, Notion, and messaging to complete real work with human approval boundaries and durable memory.",
      },
      {
        name: "Workflow Toolkit",
        details:
          "Open-source file-driven AI ops pattern with reusable skills, markdown-to-API synchronization, ticket workflows, GitHub Actions, and local SQLite state for auditable handoffs.",
      },
      {
        name: "Copilot-Ops Console",
        details:
          "Portfolio implementation showing agentic ticket lanes, skill pipelines, voice capture, worklog sync, and operator-in-the-loop automation for production support.",
      },
      {
        name: "Agentic Web Portfolio",
        details:
          "Explains the production architecture model for API-first agents using REST/GraphQL/CLI, OAuth/OIDC, scoped permissions, and composable tool access instead of brittle custom middleware.",
      },
    ],
  },
  sre: {
    title: "Site Reliability Engineer Resume",
    headline:
      "Site Reliability Engineer — Cloud Operations, Kubernetes, Observability & Incident Response",
    profile:
      "Cloud and site reliability engineer with production experience across Azure, Kubernetes, Linux operations, Infrastructure as Code, monitoring, security, disaster recovery, and cost optimization. Strong at incident response, Kusto investigations, CI/CD, runbooks, rollback planning, hybrid identity, and translating messy production constraints into reliable operating practices.",
    skills: [
      ["Reliability Engineering", "Incident response, monitoring, rollback, disaster recovery, capacity planning, operational readiness, runbooks"],
      ["Cloud Platforms", "Azure, AKS, Azure Front Door, Azure Automation, Logic Apps, Microsoft Fabric, hybrid cloud operations"],
      ["Observability & Security", "Log Analytics, Kusto/KQL, Microsoft Sentinel, SIEM, vulnerability scanning, AMA/MMA agents, Data Collection Rules"],
      ["Infrastructure & Delivery", "Bicep, ARM templates, CI/CD, GitHub Actions, Kubernetes, Docker, Linux, PowerShell, Bash, SQL"],
      ["Identity & Networking", "Entra ID, MFA, PIM, RBAC, Azure AD Connect, VNETs, subnets, VPN, NSGs, Azure Firewall"],
    ],
    certs: ["AZ-500 · AZ-305 · AZ-104 · DP-900", "CKA · CCNA · CompTIA Server+"],
    experience: resumeExperience,
    projects: [
      {
        name: "Infrastructure as Code Portfolio",
        details:
          "Interactive portfolio and articles showing Azure deployment pipelines, Bicep/AVM patterns, GitHub Actions, OIDC, policy governance, and repeatable cloud delivery.",
      },
      {
        name: "Log and Operations Portfolio",
        details:
          "Operational demos covering KQL, log pipelines, Azure monitoring, incident investigation, and production support practices.",
      },
      {
        name: "Fabric Capacity and FinOps Work",
        details:
          "Cost and capacity optimization examples for Microsoft Fabric and Azure, including SKU selection, pause/resume strategy, and operational guardrails.",
      },
    ],
  },
};
