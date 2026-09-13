"use strict";

/*
============================================================
STUDENT INNOVATION PORTAL
SHARED APPLICATION ENGINE
============================================================

Features:
- JWT Authentication
- Email + Phone Login
- Student Idea Submission
- Student My Ideas
- Student Idea Details
- Student Edit Idea
- Student Delete Idea
- Faculty/Admin All Ideas
- Faculty/Admin Status Update
- Comments
- PortalStore
- Dark / Light Mode
- Student / Admin View
- Toast Notifications

Backend:
http://localhost:8080/api

IMPORTANT:
Include this file ONLY ONCE in each HTML page.
============================================================
*/


/* ============================================================
   API CONFIGURATION
============================================================ */

const API_BASE_URL = "https://idea-nd9r.onrender.com/api";


/* ============================================================
   AUTHENTICATION
============================================================ */

function getToken() {
    return localStorage.getItem("token");
}


function isLoggedIn() {
    return !!getToken();
}


function getLoggedInUser() {
    return {
        id: localStorage.getItem("userId"),
        email: localStorage.getItem("userEmail") || localStorage.getItem("email"),
        role: localStorage.getItem("userRole") || localStorage.getItem("role"),
        name: localStorage.getItem("userName") || localStorage.getItem("name"),
        phoneNumber: localStorage.getItem("userPhone") || localStorage.getItem("phoneNumber"),
        department: localStorage.getItem("userDepartment") || localStorage.getItem("department")
    };
}


function logoutUser() {

    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("userEmail");
    localStorage.removeItem("userRole");
    localStorage.removeItem("userName");
    localStorage.removeItem("userPhone");

    if (window.portalStore) {
        window.portalStore.setCurrentUser(null);
    }

    window.location.href = "login.html";
}


/* ============================================================
   LOGIN
============================================================ */

async function loginUser(email, phoneNumber) {

    try {

        const response = await fetch(
            API_BASE_URL + "/auth/login",
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    email: email,
                    phoneNumber: phoneNumber
                })
            }
        );

        let data = {};

        try {
            data = await response.json();
        } catch (error) {
            console.warn("Server response is not JSON.");
        }

        if (!response.ok) {

            throw new Error(
                data.message ||
                data.error ||
                "Invalid email or phone number"
            );
        }

        if (!data.token) {

            throw new Error(
                "Login successful, but JWT token was not received."
            );
        }

        /* Save JWT */

        localStorage.setItem(
            "token",
            data.token
        );


        /* Save user ID */

        if (
            data.id !== undefined &&
            data.id !== null
        ) {

            localStorage.setItem(
                "userId",
                String(data.id)
            );
        }


        /* Save email */

        if (data.email) {

            localStorage.setItem(
                "userEmail",
                data.email
            );
        }


        /* Save role */

        if (data.role) {

            localStorage.setItem(
                "userRole",
                String(data.role).toUpperCase()
            );
        }


        /* Save name */

        if (
            data.name ||
            data.fullName ||
            data.username
        ) {

            localStorage.setItem(
                "userName",
                data.name ||
                data.fullName ||
                data.username
            );
        }


        /* Save phone */

        if (
            data.phoneNumber ||
            data.phone
        ) {

            localStorage.setItem(
                "userPhone",
                data.phoneNumber ||
                data.phone
            );
        }

        localStorage.setItem(
            "userDepartment",
            data.department || "General"
        );


        /* Update PortalStore */

        if (window.portalStore) {

            window.portalStore.setCurrentUser({

                id: data.id || null,

                email: data.email || "",

                role: String(
                    data.role || "STUDENT"
                ).toLowerCase(),

                name:
                    data.name ||
                    data.fullName ||
                    data.username ||
                    data.email ||
                    "Student",

                phoneNumber:
                    data.phoneNumber ||
                    data.phone ||
                    ""
            });
        }


        return data;

    } catch (error) {

        console.error(
            "Login error:",
            error
        );

        throw error;
    }
}


/* ============================================================
   API REQUEST HELPER
============================================================ */

async function apiRequest(
    url,
    options
) {

    options = options || {};

    const token = getToken();

    const headers = {};

    /*
    Copy existing headers
    */

    if (options.headers) {

        Object.keys(options.headers).forEach(
            function (key) {

                headers[key] =
                    options.headers[key];

            }
        );
    }


    /*
    JWT
    */

    if (token) {

        headers["Authorization"] =
            "Bearer " + token;
    }


    /*
    Automatically convert object body to JSON
    */

    let requestBody =
        options.body;


    if (
        requestBody &&
        typeof requestBody === "object" &&
        !(requestBody instanceof FormData)
    ) {

        if (!headers["Content-Type"]) {

            headers["Content-Type"] =
                "application/json";
        }


        requestBody =
            JSON.stringify(requestBody);
    }


    let response;


    try {

        response =
            await fetch(
                url,
                {
                    ...options,
                    headers: headers,
                    body: requestBody
                }
            );

    } catch (error) {

        console.error(
            "Network error:",
            error
        );

        throw new Error(
            "Cannot connect to backend server. Make sure Spring Boot is running on port 8080."
        );
    }


    /*
    Unauthorized
    */

    if (response.status === 401) {

        logoutUser();

        throw new Error(
            "Your session has expired. Please login again."
        );
    }


    /*
    Forbidden
    */

    if (response.status === 403) {

        throw new Error(
            "You do not have permission to perform this action."
        );
    }


    /*
    Read response
    */

    let data = null;

    const contentType =
        response.headers.get(
            "content-type"
        ) || "";


    try {

        if (
            contentType.indexOf(
                "application/json"
            ) !== -1
        ) {

            data =
                await response.json();

        } else {

            const text =
                await response.text();

            data =
                text || null;
        }

    } catch (error) {

        console.warn(
            "Could not read server response.",
            error
        );
    }


    /*
    API error
    */

    if (!response.ok) {

        let message =
            "Request failed (" +
            response.status +
            ")";


        if (
            data &&
            typeof data === "object"
        ) {

            message =
                data.message ||
                data.error ||
                data.detail ||
                message;

        } else if (
            typeof data === "string" &&
            data.trim()
        ) {

            message = data;
        }


        throw new Error(
            message
        );
    }


    return data;
}


/* ============================================================
   STUDENT - SUBMIT IDEA
============================================================ */

async function submitIdea(
    title,
    description,
    category,
    department
) {
    return await apiRequest(
        API_BASE_URL + "/ideas",
        {
            method: "POST",
            body: {
                title: title,
                description: description,
                category: category,
                department: department
            }
        }
    );
}


/* ============================================================
   STUDENT - GET MY IDEAS
============================================================ */

async function getMyIdeas() {

    const data =
        await apiRequest(
            API_BASE_URL +
            "/ideas/my-ideas",
            {
                method: "GET"
            }
        );


    /*
    Backend may return:
    []
    OR
    { content: [] }
    OR
    { ideas: [] }
    */

    if (Array.isArray(data)) {

        return data;
    }


    if (
        data &&
        Array.isArray(data.content)
    ) {

        return data.content;
    }


    if (
        data &&
        Array.isArray(data.ideas)
    ) {

        return data.ideas;
    }


    return [];
}


/* ============================================================
   GET ONE IDEA
============================================================ */

async function getIdeaById(
    ideaId
) {

    if (
        ideaId === undefined ||
        ideaId === null ||
        ideaId === ""
    ) {

        throw new Error(
            "Idea ID is required."
        );
    }


    return await apiRequest(
        API_BASE_URL +
        "/ideas/" +
        encodeURIComponent(
            ideaId
        ),
        {
            method: "GET"
        }
    );
}


/* ============================================================
   STUDENT - EDIT IDEA
============================================================ */

async function updateMyIdea(
    ideaId,
    title,
    description,
    category,
    department
) {

    if (
        ideaId === undefined ||
        ideaId === null ||
        ideaId === ""
    ) {

        throw new Error(
            "Idea ID is required."
        );
    }


    return await apiRequest(
        API_BASE_URL +
        "/ideas/" +
        encodeURIComponent(
            ideaId
        ),
        {
            method: "PUT",

            body: {
                title: title,
                description: description,
                category: category,
                department: department
            }
        }
    );
}


/* ============================================================
   STUDENT - DELETE IDEA
============================================================ */

async function deleteMyIdea(
    ideaId
) {

    if (
        ideaId === undefined ||
        ideaId === null ||
        ideaId === ""
    ) {

        throw new Error(
            "Idea ID is required."
        );
    }


    return await apiRequest(
        API_BASE_URL +
        "/ideas/" +
        encodeURIComponent(
            ideaId
        ),
        {
            method: "DELETE"
        }
    );
}


/*
Compatibility function.

Your student-dashboard may call:
deleteIdea()

So we provide it too.
*/

async function deleteIdea(
    ideaId
) {

    return await deleteMyIdea(
        ideaId
    );
}


/* ============================================================
   ADMIN - IDEAS
============================================================ */

async function getAdminIdeasFromDashboard() {
    return await getAdminIdeas();
}

async function getAdminIdeaByIdFromDashboard(ideaId) {
    return await getAdminIdeaById(ideaId);
}

async function adminUpdateIdeaStatus(ideaId, status) {
    return await updateIdeaStatus(ideaId, status);
}

async function addAdminCommentForDashboard(ideaId, commentText) {
    return await addAdminComment(ideaId, commentText);
}

/* ============================================================
   ADMIN - GET ALL IDEAS
============================================================ */

async function getAdminIdeas() {

    const data =
        await apiRequest(
            API_BASE_URL +
            "/admin/ideas",
            {
                method: "GET"
            }
        );


    if (Array.isArray(data)) {

        return data;
    }


    if (
        data &&
        Array.isArray(data.content)
    ) {

        return data.content;
    }


    if (
        data &&
        Array.isArray(data.ideas)
    ) {

        return data.ideas;
    }


    return [];
}


/* ============================================================
   ADMIN - GET ONE IDEA
============================================================ */

async function getAdminIdeaById(
    ideaId
) {
    return await apiRequest(
        API_BASE_URL + "/admin/ideas/" + encodeURIComponent(ideaId),
        { method: "GET" }
    );
}


/* ============================================================
   ADMIN - UPDATE STATUS
============================================================ */

async function updateIdeaStatus(
    ideaId,
    status
) {

    if (
        ideaId === undefined ||
        ideaId === null ||
        ideaId === ""
    ) {

        throw new Error(
            "Idea ID is required."
        );
    }


    if (!status) {

        throw new Error(
            "Status is required."
        );
    }


    return await apiRequest(
        API_BASE_URL +
        "/admin/ideas/" +
        encodeURIComponent(
            ideaId
        ) +
        "/status",
        {
            method: "PATCH",

            body: {
                status: status
            }
        }
    );
}


/* ============================================================
   COMMENTS - GET
============================================================ */

async function getComments(
    ideaId
) {

    const data =
        await apiRequest(
            API_BASE_URL +
            "/ideas/" +
            encodeURIComponent(
                ideaId
            ) +
            "/comments",
            {
                method: "GET"
            }
        );


    if (Array.isArray(data)) {

        return data;
    }


    if (
        data &&
        Array.isArray(data.comments)
    ) {

        return data.comments;
    }


    return [];
}


/* ============================================================
   ADMIN - ADD COMMENT
============================================================ */

async function addAdminComment(
    ideaId,
    commentText
) {

    if (
        !commentText ||
        !commentText.trim()
    ) {

        throw new Error(
            "Comment cannot be empty."
        );
    }


    return await apiRequest(
        API_BASE_URL +
        "/admin/ideas/" +
        encodeURIComponent(
            ideaId
        ) +
        "/comments",
        {
            method: "POST",

            body: {
                commentText:
                    commentText.trim()
            }
        }
    );
}


/* ============================================================
   STATUS HELPERS
============================================================ */

function normalizeIdeaStatus(
    status
) {

    if (!status) {

        return "SUBMITTED";
    }


    let value =
        String(status)
            .trim()
            .toUpperCase()
            .replace(/[\s-]+/g, "_");


    if (value === "APPROVED") {

        return "ACCEPTED";
    }


    if (
        value === "NEEDS_WORK" ||
        value === "NEEDS_REVISION" ||
        value === "NEEDS_IMPROVEMENT"
    ) {

        return "REJECTED";
    }


    if (value === "PENDING") {

        return "UNDER_REVIEW";
    }


    return value;
}


function getStatusText(
    status
) {

    const value =
        normalizeIdeaStatus(
            status
        );


    switch (value) {

        case "SUBMITTED":
            return "Submitted";

        case "UNDER_REVIEW":
            return "Under Review";

        case "SHORTLISTED":
            return "Shortlisted";

        case "ACCEPTED":
            return "Approved";

        case "REJECTED":
            return "Needs Work";

        default:
            return status ||
                "Unknown";
    }
}


function getStatusBadgeClass(
    status
) {

    const value =
        normalizeIdeaStatus(
            status
        );


    switch (value) {

        case "ACCEPTED":

            return "bg-[#D1FAE5] text-[#2D6A4F]";

        case "REJECTED":

            return "bg-[#FEE2E2] text-[#991B1B]";

        case "SHORTLISTED":

            return "bg-[#DBEAFE] text-[#1D4ED8]";

        case "UNDER_REVIEW":

            return "bg-[#FEF3C7] text-[#92400E]";

        case "SUBMITTED":

            return "bg-[#E0E7FF] text-[#3730A3]";

        default:

            return "bg-[#F1F5F9] text-[#475569]";
    }
}


/* ============================================================
   IDEA DATA HELPERS
============================================================ */

function getIdeaTitle(
    idea
) {

    if (!idea) {
        return "Untitled Idea";
    }


    return (
        idea.title ||
        idea.productIdea ||
        idea.name ||
        "Untitled Idea"
    );
}


function getIdeaDescription(
    idea
) {

    if (!idea) {
        return "No description available.";
    }


    return (
        idea.description ||
        idea.problemStatement ||
        idea.solution ||
        "No description available."
    );
}


function getIdeaCategory(
    idea
) {

    if (!idea) {
        return "General";
    }


    return (
        idea.category ||
        idea.domain ||
        "General"
    );
}


function getIdeaDate(
    idea
) {

    if (!idea) {
        return "Unknown date";
    }


    const value =
        idea.createdAt ||
        idea.submittedAt ||
        idea.submittedDate ||
        idea.createdDate;


    if (!value) {

        return "Unknown date";
    }


    const date =
        new Date(value);


    if (
        Number.isNaN(
            date.getTime()
        )
    ) {

        return String(value);
    }


    return date.toLocaleDateString(
        "en-IN",
        {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }
    );
}


/* ============================================================
   SEED DATA
============================================================ */

const SEED_DATA = {

    currentUser: null,

    theme: "light",

    ideas: []
};


/* ============================================================
   PORTAL STORE
============================================================ */

class PortalStore {

    constructor() {

        this.init();
    }


    init() {

        const saved =
            localStorage.getItem(
                "sip_portal_data"
            );


        if (!saved) {

            this.data = {

                currentUser:
                    SEED_DATA.currentUser,

                theme:
                    SEED_DATA.theme,

                ideas:
                    []
            };

            this.save();

        } else {

            try {

                this.data =
                    JSON.parse(
                        saved
                    );

            } catch (error) {

                console.error(
                    "Invalid portal data.",
                    error
                );

                this.data = {

                    currentUser:
                        null,

                    theme:
                        "light",

                    ideas:
                        []
                };

                this.save();
            }
        }


        if (!this.data) {

            this.data = {

                currentUser:
                    null,

                theme:
                    "light",

                ideas:
                    []
            };
        }


        if (
            !Array.isArray(
                this.data.ideas
            )
        ) {

            this.data.ideas = [];
        }


        /*
        Sync backend user
        */

        const backendUser =
            getLoggedInUser();


        if (
            backendUser.id ||
            backendUser.email
        ) {

            this.data.currentUser = {

                id:
                    backendUser.id,

                email:
                    backendUser.email,

                role:
                    String(
                        backendUser.role ||
                        "STUDENT"
                    ).toLowerCase(),

                name:
                    backendUser.name ||
                    backendUser.email ||
                    "Student",

                phoneNumber:
                    backendUser.phoneNumber ||
                    ""
            };


            this.save();
        }
    }


    save() {

        localStorage.setItem(
            "sip_portal_data",
            JSON.stringify(
                this.data
            )
        );
    }


    getIdeas() {

        return this.data.ideas || [];
    }


    getIdeaById(
        id
    ) {

        return this.getIdeas()
            .find(
                function (idea) {

                    return String(
                        idea.id
                    ) ===
                    String(id);

                }
            );
    }


    addIdea(
        idea
    ) {

        const currentUser =
            this.getCurrentUser() ||
            {};


        const newIdea = {

            id:
                "SIP-" +
                new Date().getFullYear() +
                "-" +
                Math.floor(
                    100 +
                    Math.random() *
                    900
                ),

            submittedDate:
                new Date().toLocaleDateString(
                    "en-IN",
                    {
                        day: "2-digit",
                        month: "short",
                        year: "numeric"
                    }
                ),

            createdAt:
                new Date().toISOString(),

            studentName:
                currentUser.name ||
                currentUser.email ||
                "Student",

            studentEmail:
                currentUser.email ||
                "",

            status:
                "UNDER_REVIEW",

            reviews: [],

            attachments: [],

            ...idea
        };


        this.data.ideas.unshift(
            newIdea
        );


        this.save();


        return newIdea;
    }


    updateIdea(
        id,
        updatedData
    ) {

        const idea =
            this.getIdeaById(
                id
            );


        if (!idea) {

            return null;
        }


        Object.assign(
            idea,
            updatedData
        );


        this.save();


        return idea;
    }


    deleteIdea(
        id
    ) {

        const index =
            this.data.ideas.findIndex(
                function (idea) {

                    return String(
                        idea.id
                    ) ===
                    String(id);

                }
            );


        if (index === -1) {

            return false;
        }


        this.data.ideas.splice(
            index,
            1
        );


        this.save();


        return true;
    }


    updateIdeaStatus(
        id,
        status,
        comment,
        facultyName
    ) {

        facultyName =
            facultyName ||
            "Faculty";


        const idea =
            this.getIdeaById(
                id
            );


        if (!idea) {

            return null;
        }


        idea.status =
            status;


        if (comment) {

            if (!idea.reviews) {

                idea.reviews = [];
            }


            idea.reviews.unshift({

                faculty:
                    facultyName,

                comment:
                    comment,

                date:
                    new Date().toLocaleDateString(
                        "en-IN",
                        {
                            day: "2-digit",
                            month: "short",
                            year: "numeric"
                        }
                    ),

                decision:
                    status
            });
        }


        this.save();


        return idea;
    }


    getCurrentUser() {

        return this.data.currentUser;
    }


    setCurrentUser(
        user
    ) {

        this.data.currentUser =
            user;

        this.save();
    }


    getTheme() {

        return (
            this.data.theme ||
            "light"
        );
    }


    setTheme(
        theme
    ) {

        this.data.theme =
            theme;

        localStorage.setItem("sip_theme", theme);
        this.save();

        this.applyTheme();
    }


    applyTheme() {

        if (
            this.data.theme ===
            "dark"
        ) {

            document.documentElement.classList.add(
                "dark"
            );

            document.documentElement.classList.remove(
                "light"
            );

        } else {

            document.documentElement.classList.remove(
                "dark"
            );

            document.documentElement.classList.add(
                "light"
            );
        }
    }
}


/* ============================================================
   CREATE PORTAL STORE
============================================================ */

if (!window.portalStore) {

    window.portalStore =
        new PortalStore();
}


/* ============================================================
   TOAST CONTAINER
============================================================ */

function initToastContainer() {

    if (
        document.getElementById(
            "toast-container"
        )
    ) {

        return;
    }


    const container =
        document.createElement(
            "div"
        );


    container.id =
        "toast-container";


    container.className =
        "fixed bottom-5 right-5 z-[200] flex flex-col gap-2 pointer-events-none";


    document.body.appendChild(
        container
    );
}


/* ============================================================
   HTML ESCAPE
============================================================ */

function escapeHTML(
    value
) {

    const div =
        document.createElement(
            "div"
        );


    div.textContent =
        value === null ||
        value === undefined
            ? ""
            : String(value);


    return div.innerHTML;
}


/* ============================================================
   TOAST
============================================================ */

function showToast(
    message,
    type
) {

    type =
        type ||
        "info";


    initToastContainer();


    const container =
        document.getElementById(
            "toast-container"
        );


    if (!container) {

        return;
    }


    const toast =
        document.createElement(
            "div"
        );


    let colorClass =
        "bg-primary text-on-primary";


    let icon =
        "info";


    if (
        type === "success"
    ) {

        colorClass =
            "bg-[#2D6A4F] text-white";

        icon =
            "check_circle";

    } else if (
        type === "error"
    ) {

        colorClass =
            "bg-error text-on-error";

        icon =
            "error";

    } else if (
        type === "warning"
    ) {

        colorClass =
            "bg-[#92400E] text-white";

        icon =
            "warning";
    }


    toast.className =
        "toast-item " +
        "pointer-events-auto " +
        "flex items-center gap-2 " +
        "px-4 py-3 rounded-lg shadow-lg " +
        "text-sm font-medium " +
        "transition-all duration-300 " +
        "translate-y-4 opacity-0 " +
        colorClass;


    toast.innerHTML =
        '<span class="material-symbols-outlined text-[18px]">' +
        escapeHTML(icon) +
        "</span>" +
        "<span>" +
        escapeHTML(message) +
        "</span>";


    container.appendChild(
        toast
    );


    setTimeout(
        function () {

            toast.classList.remove(
                "translate-y-4",
                "opacity-0"
            );

        },
        10
    );


    setTimeout(
        function () {

            toast.classList.add(
                "opacity-0",
                "translate-y-2"
            );


            setTimeout(
                function () {

                    toast.remove();

                },
                300
            );

        },
        3500
    );
}


/* ============================================================
   THEME TOGGLE
============================================================ */

function initThemeToggle() {

    const header =
        document.querySelector(
            "header"
        );


    if (!header) {

        return;
    }


    let actionGroup =
        header.querySelector(
            ".header-action-group"
        );


    if (!actionGroup) {

        actionGroup =
            document.createElement(
                "div"
            );


        actionGroup.className =
            "header-action-group flex items-center gap-2";


        header.appendChild(
            actionGroup
        );
    }


    if (
        actionGroup.querySelector(
            ".portal-theme-button"
        )
    ) {

        return;
    }


    const currentTheme =
        window.portalStore.getTheme();


    const themeBtn =
        document.createElement(
            "button"
        );


    themeBtn.type =
        "button";


    themeBtn.className =
        "portal-theme-button p-2 rounded-full " +
        "text-on-surface-variant " +
        "hover:bg-surface-container " +
        "transition-colors " +
        "flex items-center justify-center";


    themeBtn.setAttribute(
        "aria-label",
        "Toggle Dark Mode"
    );


    themeBtn.innerHTML =
        '<span class="material-symbols-outlined">' +
        (
            currentTheme === "dark"
                ? "light_mode"
                : "dark_mode"
        ) +
        "</span>";


    themeBtn.addEventListener(
        "click",
        function () {

            const current =
                window.portalStore.getTheme();


            const newTheme =
                current === "dark"
                    ? "light"
                    : "dark";


            window.portalStore.setTheme(
                newTheme
            );


            themeBtn.innerHTML =
                '<span class="material-symbols-outlined">' +
                (
                    newTheme === "dark"
                        ? "light_mode"
                        : "dark_mode"
                ) +
                "</span>";


            showToast(
                "Switched to " +
                newTheme +
                " mode",
                "success"
            );
        }
    );


    actionGroup.appendChild(
        themeBtn
    );
}


/* ============================================================
   PERSONA SWITCHER
============================================================ */

function initPersonaSwitcher() {
    const actionGroup = document.querySelector("header .header-action-group");
    if (!actionGroup || actionGroup.querySelector(".persona-switcher")) return;

    const role = String(
        localStorage.getItem("userRole") || "STUDENT"
    ).toUpperCase();

    const isAdminUser = role === "ADMIN";
    const button = document.createElement("button");

    button.type = "button";
    button.className =
        "persona-switcher px-3 py-1 text-xs font-semibold rounded-full border " +
        "transition-all flex items-center gap-1";
    button.title = "Open dashboard";
    button.innerHTML =
        '<span class="material-symbols-outlined text-[16px]">' +
        (isAdminUser ? "admin_panel_settings" : "person") +
        "</span>" +
        (isAdminUser ? "Admin View" : "Student View");

    button.addEventListener("click", function () {
        window.location.href = isAdminUser
            ? "admin-dashboard.html"
            : "student-dashboard.html";
    });

    actionGroup.prepend(button);
}

/* ============================================================
   DOM READY
============================================================ */

document.addEventListener(
    "DOMContentLoaded",
    function () {

        refreshProfileWidgets();

        if (window.portalStore) {
            const savedTheme = localStorage.getItem("sip_theme");
            if (savedTheme === "dark" || savedTheme === "light") {
                window.portalStore.data.theme = savedTheme;
            }
            window.portalStore.applyTheme();
        }


        initThemeToggle();

        initPersonaSwitcher();

        initToastContainer();
    }
);


/* ============================================================
   LOGIN PROTECTION
============================================================ */

function requireLogin() {

    if (!isLoggedIn()) {

        window.location.href =
            "login.html";

        return false;
    }


    return true;
}


/* ============================================================
   STUDENT ROLE
============================================================ */

function isStudent() {

    const role =
        String(
            localStorage.getItem(
                "userRole"
            ) || ""
        ).toUpperCase();


    return role === "STUDENT";
}


/* ============================================================
   FACULTY ROLE
============================================================ */

function isAdmin() {
    return String(localStorage.getItem("userRole") || "").toUpperCase() === "ADMIN";
}

function requireAdmin() {
    if (!requireLogin()) return false;
    if (!isAdmin()) {
        showToast("Admin access required.", "error");
        return false;
    }
    return true;
}

async function getMyProfile() {
    return await apiRequest(
        API_BASE_URL + "/profile/me",
        { method: "GET" }
    );
}

/* ============================================================
   GLOBAL EXPORTS
============================================================ */

/*
IMPORTANT:
These exports are what student-dashboard.html uses.
*/

window.getToken =
    getToken;

window.isLoggedIn =
    isLoggedIn;

window.getLoggedInUser =
    getLoggedInUser;

window.loginUser =
    loginUser;

window.logoutUser =
    logoutUser;

window.apiRequest =
    apiRequest;


/* Student */

window.submitIdea =
    submitIdea;

window.getMyIdeas =
    getMyIdeas;

window.getIdeaById =
    getIdeaById;

window.getMyProfile =
    getMyProfile;

window.updateMyIdea =
    updateMyIdea;

window.deleteMyIdea =
    deleteMyIdea;


/*
Compatibility:
student-dashboard.html may call deleteIdea()
*/

window.deleteIdea =
    deleteIdea;


/* Admin */

window.getAdminIdeas =
    getAdminIdeas;

window.getAdminIdeasFromDashboard = getAdminIdeasFromDashboard;
window.getAdminIdeaByIdFromDashboard = getAdminIdeaByIdFromDashboard;
window.adminUpdateIdeaStatus = adminUpdateIdeaStatus;
window.addAdminCommentForDashboard = addAdminCommentForDashboard;

window.getAdminIdeaById =
    getAdminIdeaById;

window.updateIdeaStatus =
    updateIdeaStatus;


/* Comments */

window.getComments =
    getComments;

window.addAdminComment =
    addAdminComment;


/* Helpers */

window.normalizeIdeaStatus =
    normalizeIdeaStatus;

window.getStatusText =
    getStatusText;

window.getStatusBadgeClass =
    getStatusBadgeClass;

window.getIdeaTitle =
    getIdeaTitle;

window.getIdeaDescription =
    getIdeaDescription;

window.getIdeaCategory =
    getIdeaCategory;

window.getIdeaDate =
    getIdeaDate;


/* UI */

window.showToast =
    showToast;

window.escapeHTML =
    escapeHTML;


/* Access */

window.requireLogin =
    requireLogin;

window.requireStudent =
    requireStudent;

window.requireAdmin =
    requireAdmin;

window.isStudent =
    isStudent;

window.isAdmin =
    isAdmin;


async function refreshProfileWidgets() {
    if (!isLoggedIn()) return;
    try {
        const profile = await getMyProfile();
        localStorage.setItem("userName", profile.name || profile.email || "User");
        localStorage.setItem("userProfilePhoto", profile.profilePhoto || "");
        localStorage.setItem("userPhone", profile.phoneNumber || "");
        localStorage.setItem("userDepartment", profile.department || "General");
        document.querySelectorAll("[data-profile-name]").forEach(el => el.textContent = profile.name || profile.email || "User");
        document.querySelectorAll("[data-profile-email]").forEach(el => el.textContent = profile.email || "");
        document.querySelectorAll("[data-profile-photo]").forEach(el => {
            if (profile.profilePhoto) el.src = profile.profilePhoto;
        });
    } catch (e) { console.debug("Profile widget refresh skipped:", e.message); }
}

/* ============================================================
   DEBUG CHECK
============================================================ */

console.log(
    "Student Innovation Portal app.js loaded successfully."
);

console.log(
    "getMyIdeas available:",
    typeof window.getMyIdeas
);

console.log(
    "deleteIdea available:",
    typeof window.deleteIdea
);

console.log(
    "updateMyIdea available:",
    typeof window.updateMyIdea
);