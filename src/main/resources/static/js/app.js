const API_BASE = "";

// Global JWT Fetch Interceptor
const originalFetch = window.fetch;
window.fetch = function (url, options) {
    options = options || {};
    options.headers = options.headers || {};

    let token = null;
    if (url.includes("/api/admin")) {
        token = localStorage.getItem("eventhub_admin_token");
    } else if (url.includes("/api/departments")) {
        const deptSession = localStorage.getItem("eventhub_dept_session");
        if (deptSession) {
            try {
                token = JSON.parse(deptSession).token;
            } catch (e) {}
        }
    } else if (url.includes("/api/student")) {
        const studentSession = localStorage.getItem("eventhub_student_session");
        if (studentSession) {
            try {
                token = JSON.parse(studentSession).token;
            } catch (e) {}
        }
    }
    
    if (token) {
        if (options.headers instanceof Headers) {
            options.headers.set("Authorization", "Bearer " + token);
        } else if (Array.isArray(options.headers)) {
            options.headers.push(["Authorization", "Bearer " + token]);
        } else {
            options.headers["Authorization"] = "Bearer " + token;
        }
    }
    
    return originalFetch(url, options).then(response => {
        if ((response.status === 401 || response.status === 403) && !url.includes("/login")) {
            console.warn("Unauthorized access to " + url);
        }
        return response;
    });
};

// Format ISO date string to readable format
function formatEventDate(dateTimeStr) {
    if (!dateTimeStr) return "";
    const date = new Date(dateTimeStr);
    return date.toLocaleDateString('en-US', {
        weekday: 'short',
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    }) + ' at ' + date.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Generate a premium fallback gradient based on string hashing (for event names)
function getEventPosterFallback(eventName) {
    const gradients = [
        "linear-gradient(135deg, #4f46e5, #06b6d4)", // Indigo -> Cyan
        "linear-gradient(135deg, #ec4899, #f43f5e)", // Pink -> Rose
        "linear-gradient(135deg, #8b5cf6, #d946ef)", // Violet -> Fuchsia
        "linear-gradient(135deg, #f59e0b, #e11d48)", // Amber -> Rose
        "linear-gradient(135deg, #10b981, #3b82f6)", // Emerald -> Blue
        "linear-gradient(135deg, #3b82f6, #8b5cf6)"  // Blue -> Violet
    ];
    
    // Hash function to choose a gradient deterministically
    let hash = 0;
    for (let i = 0; i < eventName.length; i++) {
        hash = eventName.charCodeAt(i) + ((hash << 5) - hash);
    }
    const index = Math.abs(hash) % gradients.length;
    return gradients[index];
}

// Set Event Poster on Image elements
function setEventPoster(imgElement, eventPosterUrl, eventName) {
    if (eventPosterUrl && eventPosterUrl.startsWith("/uploads/") && eventPosterUrl !== "/uploads/xenon.jpg" && eventPosterUrl !== "/uploads/rhythms.jpg" && eventPosterUrl !== "/uploads/sports.jpg" && eventPosterUrl !== "/uploads/hackit.jpg" && eventPosterUrl !== "/uploads/aidebug.jpg" && eventPosterUrl !== "/uploads/iothack.jpg") {
        imgElement.src = eventPosterUrl;
        imgElement.onerror = () => {
            imgElement.style.display = "none";
            const parent = imgElement.parentElement;
            parent.style.background = getEventPosterFallback(eventName);
            parent.innerHTML += `<div class="fallback-poster-text">${eventName}</div>`;
        };
    } else {
        // Since we don't have actual files, we can generate a beautiful CSS gradient block
        imgElement.style.display = "none";
        const parent = imgElement.parentElement;
        parent.style.background = getEventPosterFallback(eventName);
        parent.style.display = "flex";
        parent.style.alignItems = "center";
        parent.style.justifyContent = "center";
        parent.style.padding = "20px";
        parent.style.color = "#fff";
        parent.style.fontWeight = "800";
        parent.style.fontSize = "1.25rem";
        parent.style.textAlign = "center";
        parent.innerHTML = `<span class="px-2">${eventName}</span>`;
    }
}

// Notification Drawer and Real-time Polling
let lastNotificationCount = -1; // -1 indicates page initial load to prevent toasts for existing notices
let fetchedNotificationsList = [];

function startNotificationPolling() {
    // Poll immediately
    pollNotifications();
    // Poll every 5 seconds
    setInterval(pollNotifications, 5000);

    // Add offcanvas listener to mark notices as read
    const drawerEl = document.getElementById("notificationDrawer");
    if (drawerEl) {
        drawerEl.addEventListener("show.bs.offcanvas", () => {
            let readNotifs = JSON.parse(localStorage.getItem("eventhub_read_notifs") || "[]");
            fetchedNotificationsList.forEach(n => {
                if (!readNotifs.includes(n.id)) {
                    readNotifs.push(n.id);
                }
            });
            localStorage.setItem("eventhub_read_notifs", JSON.stringify(readNotifs));
            // Re-render to clear the badges and update card styles
            renderNotificationDrawer(fetchedNotificationsList);
        });
    }
}

function pollNotifications() {
    fetch(`${API_BASE}/api/notifications`)
        .then(response => response.json())
        .then(notifications => {
            fetchedNotificationsList = notifications;
            renderNotificationDrawer(notifications);
            
            // Check for new notifications to show toast alerts (loop chronologically for all new notices)
            if (lastNotificationCount !== -1 && notifications.length > lastNotificationCount) {
                const newCount = notifications.length - lastNotificationCount;
                for (let i = newCount - 1; i >= 0; i--) {
                    if (notifications[i]) {
                        showToastNotification(notifications[i]);
                    }
                }
            }
            lastNotificationCount = notifications.length;
        })
        .catch(err => console.error("Error polling notifications:", err));
}

function renderNotificationDrawer(notifications) {
    const listContainer = document.getElementById("notif-drawer-list");
    const badgeContainer = document.getElementById("notif-badge");
    const pillBadgeContainer = document.getElementById("notif-pill-count");

    const readNotifs = JSON.parse(localStorage.getItem("eventhub_read_notifs") || "[]");
    const unreadCount = notifications.filter(n => !readNotifs.includes(n.id)).length;

    if (badgeContainer) {
        badgeContainer.innerText = unreadCount;
        badgeContainer.style.display = unreadCount > 0 ? "inline-block" : "none";
    }
    
    if (pillBadgeContainer) {
        pillBadgeContainer.innerText = unreadCount;
    }

    if (!listContainer) return;

    if (notifications.length === 0) {
        listContainer.innerHTML = '<div class="text-center text-muted p-4">No recent notices</div>';
        return;
    }

    let html = "";
    notifications.forEach(notif => {
        const isDept = notif.sender.startsWith("Department");
        const isRead = readNotifs.includes(notif.id);
        const formattedTime = new Date(notif.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        
        const clickAttr = notif.eventId ? `onclick="window.location.href='events.html?eventId=${notif.eventId}'" style="cursor: pointer;"` : '';
        const clickableClass = notif.eventId ? 'clickable-notif' : '';
        const unreadIndicator = !isRead ? '<span class="unread-dot-glow"></span>' : '';

        html += `
            <div class="notif-card ${isDept ? 'dept-notif' : ''} ${isRead ? 'read-notif' : ''} ${clickableClass}" ${clickAttr}>
                <div class="d-flex justify-content-between align-items-start mb-1">
                    <h6 class="mb-0 text-white font-weight-bold d-flex align-items-center gap-2" style="font-size: 0.95rem;">
                        ${unreadIndicator}
                        ${notif.title}
                    </h6>
                    <small class="text-muted" style="font-size: 0.75rem; flex-shrink: 0;">${formattedTime}</small>
                </div>
                <p class="mb-1 text-muted" style="font-size: 0.85rem; line-height: 1.4;">${notif.content}</p>
                <div class="d-flex justify-content-between align-items-center mt-2">
                    <span class="badge ${isDept ? 'bg-secondary' : 'bg-primary'}" style="font-size: 0.7rem;">${notif.sender}</span>
                    ${notif.eventId ? '<small class="text-secondary small fw-bold" style="font-size: 0.7rem;"><i class="fa-solid fa-arrow-right-to-bracket me-1"></i>View Event</small>' : ''}
                </div>
            </div>
        `;
    });
    listContainer.innerHTML = html;
}

function showToastNotification(notif) {
    // Create floating toast alert dynamically
    const toast = document.createElement("div");
    toast.style.position = "fixed";
    toast.style.bottom = "80px";
    toast.style.right = "20px";
    toast.style.zIndex = "2000";
    toast.style.width = "320px";
    toast.className = "glass-panel p-3 animate-fade-up";
    toast.style.borderLeft = "4px solid #6366f1";
    toast.style.background = "#0f1322";
    
    toast.innerHTML = `
        <div class="d-flex justify-content-between align-items-center mb-1">
            <strong style="color: #fff; font-size: 0.95rem;"><i class="fas fa-bell text-primary mr-2"></i> ${notif.title}</strong>
            <button type="button" class="btn-close btn-close-white" style="font-size: 0.75rem;" onclick="this.parentElement.parentElement.remove()"></button>
        </div>
        <p class="text-muted mb-0" style="font-size: 0.85rem;">${notif.content}</p>
    `;
    
    document.body.appendChild(toast);
    
    // Auto remove after 6 seconds
    setTimeout(() => {
        if (toast.parentElement) {
            toast.remove();
        }
    }, 6000);
}

// Check if user is logged in as department
function checkDepartmentAuth() {
    const deptInfo = localStorage.getItem("eventhub_dept_session");
    if (!deptInfo) {
        return null;
    }
    return JSON.parse(deptInfo);
}

// Check if user is logged in as student
function checkStudentAuth() {
    const sessionStr = localStorage.getItem("eventhub_student_session");
    if (sessionStr) {
        try {
            return JSON.parse(sessionStr);
        } catch (e) {
            localStorage.removeItem("eventhub_student_session");
        }
    }
    return null;
}

function logoutDepartment() {
    localStorage.removeItem("eventhub_dept_session");
    window.location.href = "department.html";
}

// Reusable function to toggle password input visibility and update the icon
function togglePasswordVisibility(inputId, buttonEl) {
    const input = document.getElementById(inputId);
    if (!input) return;
    const icon = buttonEl.querySelector("i");
    if (input.type === "password") {
        input.type = "text";
        if (icon) {
            icon.classList.remove("fa-eye");
            icon.classList.add("fa-eye-slash");
        }
    } else {
        input.type = "password";
        if (icon) {
            icon.classList.remove("fa-eye-slash");
            icon.classList.add("fa-eye");
        }
    }
}

// Premium success popup animation overlay
function showSuccessPopup(message) {
    // Create popup overlay
    const overlay = document.createElement("div");
    overlay.className = "success-popup-overlay";
    overlay.innerHTML = `
        <div class="success-popup-card glass-panel">
            <div class="success-popup-icon-wrapper">
                <svg class="checkmark-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
                    <circle class="checkmark-circle" cx="26" cy="26" r="25" fill="none"/>
                    <path class="checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8"/>
                </svg>
            </div>
            <h4 style="color: #fff; font-weight: 700; margin-top: 15px;">${message}</h4>
            <div class="success-popup-emoji">😊</div>
        </div>
    `;
    document.body.appendChild(overlay);

    // Auto close after 2.5 seconds (fade out first)
    setTimeout(() => {
        overlay.classList.add("fade-out");
        setTimeout(() => {
            if (overlay.parentNode) {
                overlay.remove();
            }
        }, 500);
    }, 2000);
}

// Notice Section Click Bell Shake Animation & Procedural Chime (Requirement 7)
document.addEventListener("DOMContentLoaded", () => {
    const noticeButtons = [
        document.querySelector('[data-bs-target="#notificationDrawer"]'), // Navbar Notices button
        document.querySelector('.notif-pill') // Floating notice pill
    ];
    
    noticeButtons.forEach(btn => {
        if (!btn) return;
        btn.addEventListener("click", () => {
            // Locate any bell or megaphone icon inside
            const icon = btn.querySelector(".fa-bell") || btn.querySelector(".fa-bullhorn");
            if (icon) {
                icon.classList.remove("bell-shake-animation");
                void icon.offsetWidth; // Trigger DOM reflow to restart animation
                icon.classList.add("bell-shake-animation");
                
                // Remove class after animation finishes
                setTimeout(() => {
                    icon.classList.remove("bell-shake-animation");
                }, 700);
            }
            playNoticeChime();
        });
    });
});

// Procedural high-quality notification chime using Web Audio API
function playNoticeChime() {
    try {
        const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        
        // Clean sine tone A5
        const osc1 = audioCtx.createOscillator();
        const gain1 = audioCtx.createGain();
        osc1.type = 'sine';
        osc1.frequency.setValueAtTime(880, audioCtx.currentTime); 
        gain1.gain.setValueAtTime(0.08, audioCtx.currentTime);
        gain1.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.6);
        osc1.connect(gain1);
        gain1.connect(audioCtx.destination);
        
        osc1.start();
        osc1.stop(audioCtx.currentTime + 0.65);

        // Delayed harmony note C#6
        setTimeout(() => {
            if (audioCtx.state === 'suspended') return;
            const osc2 = audioCtx.createOscillator();
            const gain2 = audioCtx.createGain();
            osc2.type = 'sine';
            osc2.frequency.setValueAtTime(1108.73, audioCtx.currentTime); 
            gain2.gain.setValueAtTime(0.06, audioCtx.currentTime);
            gain2.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.5);
            osc2.connect(gain2);
            gain2.connect(audioCtx.destination);
            
            osc2.start();
            osc2.stop(audioCtx.currentTime + 0.55);
        }, 80);
        
    } catch (e) {
        console.error("Web Audio API chime error:", e);
    }
}// Theme Toggle Helper
function initTheme() {
    const savedTheme = localStorage.getItem("eventhub_theme") || "dark";
    document.documentElement.setAttribute("data-theme", savedTheme);
    updateThemeToggleIcon(savedTheme);
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute("data-theme") || "dark";
    const newTheme = currentTheme === "dark" ? "light" : "dark";
    document.documentElement.setAttribute("data-theme", newTheme);
    localStorage.setItem("eventhub_theme", newTheme);
    updateThemeToggleIcon(newTheme);
}

function updateThemeToggleIcon(theme) {
    const icon = document.getElementById("theme-toggle-icon");
    if (!icon) return;
    if (theme === "light") {
        icon.className = "fa-solid fa-moon text-primary";
    } else {
        icon.className = "fa-solid fa-sun text-warning";
    }
}

function renderDynamicNavbar() {
    const navBarNav = document.querySelector(".navbar-nav");
    if (!navBarNav) return;

    // Check sessions
    const studentSession = localStorage.getItem("eventhub_student_session");
    const deptSession = localStorage.getItem("eventhub_dept_session");
    const adminToken = localStorage.getItem("eventhub_admin_token");

    // Get current page filename
    const path = window.location.pathname;
    const page = path.split("/").pop();

    // Remove existing link list items (i.e. li elements that have a.nav-link)
    const listItems = Array.from(navBarNav.querySelectorAll("li"));
    listItems.forEach(item => {
        if (item.querySelector("a.nav-link")) {
            item.remove();
        }
    });

    // Create the correct set of links
    const homeActive = (page === 'index.html' || page === '') ? 'active' : '';
    const eventsActive = (page === 'events.html' || page === 'register.html') ? 'active' : '';
    const loginActive = (page === 'login.html') ? 'active' : '';
    const dashboardActive = (page === 'dashboard.html' || page === 'department.html' || page === 'admin.html' || page === 'history.html') ? 'active' : '';

    let linksHtml = "";
    linksHtml += `<li class="nav-item"><a class="nav-link nav-link-custom ${homeActive}" href="index.html"><i class="fa-solid fa-house me-1"></i> Home</a></li>`;
    linksHtml += `<li class="nav-item"><a class="nav-link nav-link-custom ${eventsActive}" href="events.html"><i class="fa-solid fa-calendar-days me-1"></i> Events</a></li>`;

    if (studentSession) {
        linksHtml += `<li class="nav-item"><a class="nav-link nav-link-custom ${dashboardActive}" href="dashboard.html"><i class="fa-solid fa-circle-user me-1"></i> Student Dashboard</a></li>`;
    } else if (deptSession) {
        linksHtml += `<li class="nav-item"><a class="nav-link nav-link-custom ${dashboardActive}" href="department.html"><i class="fa-solid fa-building-columns me-1"></i> Department Workspace</a></li>`;
    } else if (adminToken) {
        linksHtml += `<li class="nav-item"><a class="nav-link nav-link-custom ${dashboardActive}" href="admin.html"><i class="fa-solid fa-lock me-1"></i> Admin Console</a></li>`;
    } else {
        linksHtml += `<li class="nav-item"><a class="nav-link nav-link-custom ${loginActive}" href="login.html"><i class="fa-solid fa-right-to-bracket me-1"></i> Login</a></li>`;
    }

    // Insert these links at the beginning of navbar-nav
    navBarNav.insertAdjacentHTML("afterbegin", linksHtml);
}

document.addEventListener("DOMContentLoaded", () => {
    initTheme();
    renderDynamicNavbar();
});
