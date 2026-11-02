import { readFile } from "node:fs/promises";
import path from "node:path";
import {
  contactLinks,
  postsByDate,
  resumeVariants,
  type PostMeta,
  type ResumeVariantKey,
} from "./data";

export type RouteResult = {
  status: number;
  headers: Record<string, string>;
  body: string | Uint8Array;
};

const securityHeaders: Record<string, string> = {
  "Strict-Transport-Security": "max-age=31536000; includeSubDomains",
  "X-Content-Type-Options": "nosniff",
  "X-Frame-Options": "SAMEORIGIN",
  "Content-Security-Policy":
    "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:; frame-ancestors 'none'",
};

function normalizePath(pathname: string): string {
  if (!pathname) return "/";
  if (pathname.length > 1 && pathname.endsWith("/")) {
    return pathname.slice(0, -1);
  }
  return pathname;
}

function escapeHtml(value: string): string {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function pageShell(title: string, body: string, options: { toc?: string; cardLayout?: boolean } = {}): string {
  const { toc, cardLayout } = options;
  return `<!DOCTYPE html><html lang="en"><head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>${escapeHtml(title)} — acestus.com</title>
    <link rel="stylesheet" href="/static/css/style.css">
    <script src="/static/js/site.js" defer></script>
  </head><body>
    <header class="site-header">
      <a href="/" class="site-name">acestus.com</a>
      <nav class="site-nav" aria-label="Site navigation">
        <a href="/blog">Blog</a>
        <a href="/portfolio">Portfolio</a>
        <a href="/contact">Contact</a>
        <a href="/resume">Resume</a>
      </nav>
      <button class="theme-toggle" type="button" data-theme-toggle aria-label="Toggle theme">
        <span class="icon-sun">☀</span><span class="icon-moon">☾</span>
      </button>
    </header>
    <main class="${cardLayout ? "card-layout" : toc ? "layout-with-toc" : "layout-single"}">
      ${toc ?? ""}
      ${cardLayout ? body : `<article class="content">${body}</article>`}
    </main>
    <footer class="site-footer"><p>acestus.com</p></footer>
  </body></html>`;
}

function htmlList(className: string, items: string[]): string {
  const cls = className ? ` class="${className}"` : "";
  return `<ul${cls}>${items.map((item) => `<li>${item}</li>`).join("")}</ul>`;
}

function skillItem([category, details]: [string, string]): string {
  return `<strong>${category}</strong> — ${details}`;
}

function jobHtml(job: { company: string; dates: string; role: string; bullets: string[] }): string {
  return `<div class="resume-job">
    <div class="resume-job-header">
      <span class="resume-company">${job.company}</span>
      <span class="resume-dates">${job.dates}</span>
    </div>
    <div class="resume-role">${job.role}</div>
    ${htmlList("", job.bullets)}
  </div>`;
}

function projectHtml(project: { name: string; details: string }): string {
  return `<div class="resume-job">
    <div class="resume-role"><strong>${project.name}</strong></div>
    <p>${project.details}</p>
  </div>`;
}

export function resumeDocPath(variant: ResumeVariantKey): string {
  switch (variant) {
    case "ai-platform":
      return "/resume/ai-platform-engineer.doc";
    case "sre":
      return "/resume/site-reliability-engineer.doc";
    default:
      return "/resume.doc";
  }
}

export function resumeDownloadFilename(variant: ResumeVariantKey): string {
  switch (variant) {
    case "ai-platform":
      return "william-weeks-balconi-ai-platform-engineer-resume.doc";
    case "sre":
      return "william-weeks-balconi-site-reliability-engineer-resume.doc";
    default:
      return "william-weeks-balconi-resume.doc";
  }
}

function resumeActions(variant: ResumeVariantKey): string {
  return `<div class="resume-actions">
    <button class="resume-print" type="button" data-print-resume aria-label="Download resume as PDF">
      <span aria-hidden="true">⬇</span> Download PDF
    </button>
    <a class="resume-print" href="${resumeDocPath(variant)}" download="${resumeDownloadFilename(variant)}" aria-label="Download resume as Word document">
      <span aria-hidden="true">⬇</span> Download Word
    </a>
  </div>`;
}

function resumeBody(variant: ResumeVariantKey): string {
  const resolved = resumeVariants[variant] ?? resumeVariants.platform;
  return `<section class="resume">
    <header class="resume-header">
      <h1>William Weeks-Balconi</h1>
      <p class="resume-title">${resolved.headline}</p>
      <p class="resume-contact">
        <a href="mailto:jump@hey.com">jump@hey.com</a> &nbsp;·&nbsp;
        <a href="https://linkedin.com/in/acestus">linkedin.com/in/acestus</a>
      </p>
    </header>

    <div class="resume-section">
      <h2>Profile</h2>
      <p>${resolved.profile}</p>
    </div>

    <div class="resume-section">
      <h2>Skills</h2>
      ${htmlList("resume-skills", resolved.skills.map(skillItem))}
    </div>

    <div class="resume-section">
      <h2>Certifications</h2>
      ${htmlList("resume-certs", resolved.certs)}
    </div>

    <div class="resume-section">
      <h2>Experience</h2>
      ${resolved.experience.map(jobHtml).join("")}
    </div>

    ${resolved.projects?.length
      ? `<div class="resume-section">
          <h2>Selected Projects</h2>
          ${resolved.projects.map(projectHtml).join("")}
        </div>`
      : ""}

    <div class="resume-section">
      <h2>Education</h2>
      <p><strong>Austin College — Sherman, Texas</strong><br>BS Computer Science &amp; BA History, 2009</p>
    </div>
  </section>`;
}

function resumePage(variant: ResumeVariantKey = "platform"): string {
  const resolved = resumeVariants[variant] ?? resumeVariants.platform;
  return pageShell(resolved.title, `${resumeActions(variant)}${resumeBody(variant)}`);
}

function resumeDoc(variant: ResumeVariantKey = "platform"): string {
  const resolved = resumeVariants[variant] ?? resumeVariants.platform;
  const resumeDocStyle =
    "body { font-family: Calibri, Arial, sans-serif; font-size: 11pt; color: #000; max-width: 7.5in; margin: 0.5in auto; }" +
    "h1 { font-size: 18pt; margin: 0 0 0.1in 0; }" +
    "h2 { font-size: 11pt; text-transform: uppercase; letter-spacing: 0.05em; color: #333; border-bottom: 1px solid #999; padding-bottom: 2pt; margin: 0.2in 0 0.1in 0; }" +
    ".resume-title { font-size: 12pt; color: #333; margin: 0 0 0.05in 0; }" +
    ".resume-contact { font-size: 10pt; color: #333; margin: 0 0 0.2in 0; }" +
    ".resume-contact a { color: #000; text-decoration: none; }" +
    ".resume-section { margin-bottom: 0.15in; }" +
    ".resume-job { margin-bottom: 0.15in; }" +
    ".resume-job-header { display: block; }" +
    ".resume-company { font-weight: bold; font-size: 11pt; }" +
    ".resume-dates { float: right; font-size: 10pt; color: #333; }" +
    ".resume-role { font-style: italic; font-size: 10.5pt; color: #333; margin-bottom: 0.05in; }" +
    "ul { margin: 0.05in 0; padding-left: 0.25in; }" +
    "li { margin-bottom: 0.03in; font-size: 10.5pt; }" +
    "strong { font-weight: bold; }";
  return `<!DOCTYPE html><html lang="en"><head>
    <meta charset="utf-8">
    <title>William Weeks-Balconi — ${resolved.title}</title>
    <style>${resumeDocStyle}</style>
  </head><body>${resumeBody(variant)}</body></html>`;
}

function landingPage(): string {
  return pageShell(
    "Home",
    `<div class="card">
      <img class="avatar-photo" src="/static/photo.png" alt="William Weeks-Balconi">
      <h1 class="card-name">William Weeks-Balconi</h1>
      <p class="card-bio">Husband, father, Christian, historian, builder.<br>
      Into Clojure, Microsoft Fabric, and writing about Infrastructure as Code.<br>
      I prefer to be contacted by email.</p>
      <a class="add-contact-btn" href="/static/contact.vcf" download="william-weeks-balconi.vcf"><span class="link-icon">👤</span><span class="link-label">Add to Contacts</span></a>
      <a class="link-btn" href="https://portfolio.acestus.com" target="_blank" rel="noopener noreferrer"><span class="link-icon">🌐</span><span class="link-label">Portfolio</span></a>
      <a class="link-btn" href="https://fabric.acestus.com" target="_blank" rel="noopener noreferrer"><span class="link-icon">📊</span><span class="link-label">Fabric</span></a>
      <a class="link-btn" href="/contact"><span class="link-icon">✉</span><span class="link-label">Contact</span></a>
      <a class="link-btn" href="/blog"><span class="link-icon">✍</span><span class="link-label">Blog</span></a>
    </div>`,
    { cardLayout: true },
  );
}

function blogIndexPage(posts: PostMeta[]): string {
  return pageShell(
    "Blog",
    `<section class="subscribe">
      <p>Read the latest posts and archive entries below.</p>
    </section>
    <h1>Posts</h1>
    <ul class="post-list">
      ${posts
        .map(
          (post) => `<li class="post-item">
            <a href="/blog/${post.slug}">${escapeHtml(post.title)}</a>
            <time>${post.date.slice(0, 10)}</time>
            <p>${escapeHtml(post.summary)}</p>
          </li>`,
        )
        .join("")}
    </ul>`,
  );
}

async function readAssetText(assetRoot: string, ...segments: string[]): Promise<string | null> {
  const filePath = path.join(assetRoot, ...segments);
  try {
    return await readFile(filePath, "utf8");
  } catch {
    return null;
  }
}

async function readAssetBytes(assetRoot: string, ...segments: string[]): Promise<Uint8Array | null> {
  const filePath = path.join(assetRoot, ...segments);
  try {
    return await readFile(filePath);
  } catch {
    return null;
  }
}

function baseHeaders(contentType: string): Record<string, string> {
  return { ...securityHeaders, "Content-Type": contentType };
}

function htmlResponse(status: number, html: string): RouteResult {
  return { status, headers: baseHeaders("text/html; charset=utf-8"), body: html };
}

function textResponse(status: number, text: string): RouteResult {
  return { status, headers: baseHeaders("text/plain; charset=utf-8"), body: text };
}

function redirectResponse(location: string): RouteResult {
  return {
    status: 302,
    headers: { ...securityHeaders, Location: location, "Content-Type": "text/plain; charset=utf-8" },
    body: `Redirecting to ${location}`,
  };
}

function binaryResponse(status: number, contentType: string, bytes: Uint8Array): RouteResult {
  return { status, headers: { ...securityHeaders, "Content-Type": contentType }, body: bytes };
}

function docResponse(filename: string, html: string): RouteResult {
  return {
    status: 200,
    headers: {
      ...securityHeaders,
      "Content-Type": "application/msword; charset=utf-8",
      "Content-Disposition": `attachment; filename="${filename}"`,
    },
    body: html,
  };
}

export async function handlePath(pathname: string, assetRoot: string): Promise<RouteResult> {
  const pathValue = normalizePath(pathname);

  if (pathValue === "/" || pathValue === "") {
    return htmlResponse(200, landingPage());
  }

  if (pathValue === "/health") {
    return textResponse(200, "ok");
  }

  if (pathValue === "/blog") {
    return htmlResponse(200, blogIndexPage(postsByDate));
  }

  if (pathValue.startsWith("/blog/")) {
    const slug = pathValue.slice("/blog/".length);
    const post = postsByDate.find((entry) => entry.slug === slug);
    if (!post) {
      return htmlResponse(404, notFoundPage());
    }

    const body = await readAssetText(assetRoot, "content", "posts", `${slug}.html`);
    if (!body) {
      return htmlResponse(404, notFoundPage());
    }

    return htmlResponse(
      200,
      pageShell(post.title, `<h1>${escapeHtml(post.title)}</h1>${body}`),
    );
  }

  if (pathValue === "/contact") {
    return htmlResponse(
      200,
      pageShell(
        "Contact",
        `<div class="card">
          <img class="avatar-photo" src="/static/photo.png" alt="William Weeks-Balconi">
          <h1 class="card-name">William Weeks-Balconi</h1>
          <p class="card-bio">Husband, father, Christian, historian, builder.<br>
          Into Clojure, Microsoft Fabric, and writing about Infrastructure as Code.<br>
          I prefer to be contacted by email.</p>
          <a class="add-contact-btn" href="/static/contact.vcf" download="william-weeks-balconi.vcf">
            <span class="link-icon">👤</span>
            <span class="link-label">Add to Contacts</span>
          </a>
          <nav class="links" aria-label="Contact links">
            ${contactLinks
              .map((link) => {
                const rel = link.rel
                  ? `noopener noreferrer ${link.rel}`
                  : link.url.startsWith("http")
                    ? "noopener noreferrer"
                    : null;
                return `<a class="link-btn" href="${link.url}"${rel ? ` rel="${rel}"` : ""}>
                  <span class="link-icon">${link.icon}</span>
                  <span class="link-text">
                    <span class="link-label">${link.label}</span>
                    <span class="link-detail">${link.detail}</span>
                  </span>
                </a>`;
              })
              .join("")}
          </nav>
        </div>`,
        { cardLayout: true },
      ),
    );
  }

  if (pathValue === "/resume") {
    return htmlResponse(200, resumePage("platform"));
  }
  if (pathValue === "/resume/ai-platform-engineer") {
    return htmlResponse(200, resumePage("ai-platform"));
  }
  if (pathValue === "/resume/site-reliability-engineer") {
    return htmlResponse(200, resumePage("sre"));
  }
  if (pathValue === "/resume.doc") {
    return docResponse("william-weeks-balconi-resume.doc", resumeDoc("platform"));
  }
  if (pathValue === "/resume/ai-platform-engineer.doc") {
    return docResponse(
      "william-weeks-balconi-ai-platform-engineer-resume.doc",
      resumeDoc("ai-platform"),
    );
  }
  if (pathValue === "/resume/site-reliability-engineer.doc") {
    return docResponse(
      "william-weeks-balconi-site-reliability-engineer-resume.doc",
      resumeDoc("sre"),
    );
  }

  if (pathValue === "/portfolio" || pathValue.startsWith("/portfolio/")) {
    const suffix = pathValue === "/portfolio" ? "" : pathValue.slice("/portfolio".length);
    return redirectResponse(`https://portfolio.acestus.com${suffix}`);
  }

  if (pathValue.startsWith("/static/")) {
    const relPath = pathValue.slice(1);
    const textExtensions = new Map([
      [".css", "text/css; charset=utf-8"],
      [".js", "application/javascript; charset=utf-8"],
      [".vcf", "text/vcard; charset=utf-8"],
      [".svg", "image/svg+xml"],
      [".html", "text/html; charset=utf-8"],
    ]);
    const binaryExtensions = new Map([
      [".png", "image/png"],
      [".jpg", "image/jpeg"],
      [".jpeg", "image/jpeg"],
    ]);

    for (const [ext, contentType] of textExtensions) {
      if (relPath.endsWith(ext)) {
        const text = await readAssetText(assetRoot, relPath.replaceAll("/", path.sep));
        if (text == null) return htmlResponse(404, notFoundPage());
        return { status: 200, headers: baseHeaders(contentType), body: text };
      }
    }

    for (const [ext, contentType] of binaryExtensions) {
      if (relPath.endsWith(ext)) {
        const bytes = await readAssetBytes(assetRoot, relPath.replaceAll("/", path.sep));
        if (bytes == null) return htmlResponse(404, notFoundPage());
        return binaryResponse(200, contentType, bytes);
      }
    }

    return htmlResponse(404, notFoundPage());
  }

  return htmlResponse(404, notFoundPage());
}

export function notFoundPage(): string {
  return pageShell("Not Found", "<h1>404</h1><p>Nothing here.</p>");
}

export { blogIndexPage, landingPage, resumeDoc, resumePage };
