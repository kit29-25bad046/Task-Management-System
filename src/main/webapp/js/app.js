/**
 * Employee Task Management System - Frontend Application Logic
 * Interacts with Java Servlets, JDBC Backend, and Collections endpoints.
 */

document.addEventListener('DOMContentLoaded', () => {
    // State
    const state = {
        currentView: 'dashboard',
        employees: [],
        tasks: [],
        priorityTasks: [],
        employeeMap: {},
        stats: {},
        editingEmployee: null,
        editingTask: null
    };

    // DOM Elements
    const elements = {
        // Navigation
        navItems: document.querySelectorAll('.nav-item'),
        viewSections: document.querySelectorAll('.view-section'),
        pageHeading: document.getElementById('pageHeading'),
        pageSubheading: document.getElementById('pageSubheading'),
        navEmpCount: document.getElementById('navEmpCount'),
        navTaskCount: document.getElementById('navTaskCount'),
        sidebarToggle: document.getElementById('sidebarToggle'),
        appSidebar: document.getElementById('appSidebar'),
        globalSearch: document.getElementById('globalSearch'),
        btnQuickAdd: document.getElementById('btnQuickAdd'),

        // Database status
        dbStatusCard: document.getElementById('dbStatusCard'),
        dbIndicator: document.getElementById('dbIndicator'),
        dbStatusLabel: document.getElementById('dbStatusLabel'),
        dbSubtext: document.getElementById('dbSubtext'),
        dbWarningBanner: document.getElementById('dbWarningBanner'),
        dbWarningText: document.getElementById('dbWarningText'),
        btnRetryDb: document.getElementById('btnRetryDb'),

        // Dashboard KPIs
        kpiEmployees: document.getElementById('kpiEmployees'),
        kpiTasks: document.getElementById('kpiTasks'),
        kpiPending: document.getElementById('kpiPending'),
        kpiInProgress: document.getElementById('kpiInProgress'),
        kpiCompleted: document.getElementById('kpiCompleted'),
        kpiOverdue: document.getElementById('kpiOverdue'),
        kpiCompletionRate: document.getElementById('kpiCompletionRate'),
        dashboardPriorityBody: document.getElementById('dashboardPriorityBody'),

        // Employees
        employeesTableBody: document.getElementById('employeesTableBody'),
        employeeSearchInput: document.getElementById('employeeSearchInput'),
        btnOpenAddEmployee: document.getElementById('btnOpenAddEmployee'),
        employeeModal: document.getElementById('employeeModal'),
        employeeForm: document.getElementById('employeeForm'),
        employeeModalTitle: document.getElementById('employeeModalTitle'),
        empModalId: document.getElementById('empModalId'),
        empModalName: document.getElementById('empModalName'),
        empModalEmail: document.getElementById('empModalEmail'),
        empModalDept: document.getElementById('empModalDept'),

        // Tasks
        allTasksTableBody: document.getElementById('allTasksTableBody'),
        taskSearchInput: document.getElementById('taskSearchInput'),
        taskPriorityFilter: document.getElementById('taskPriorityFilter'),
        taskStatusFilter: document.getElementById('taskStatusFilter'),
        btnOpenAssignTask: document.getElementById('btnOpenAssignTask'),

        // Assign Task Form
        assignTaskForm: document.getElementById('assignTaskForm'),
        taskFormId: document.getElementById('taskFormId'),
        taskFormEmployee: document.getElementById('taskFormEmployee'),
        taskFormTitle: document.getElementById('taskFormTitle'),
        taskFormDesc: document.getElementById('taskFormDesc'),
        taskFormPriority: document.getElementById('taskFormPriority'),
        taskFormDeadline: document.getElementById('taskFormDeadline'),
        taskFormStatus: document.getElementById('taskFormStatus'),
        btnCancelAssign: document.getElementById('btnCancelAssign'),

        // Priority Queue View
        priorityQueueBody: document.getElementById('priorityQueueBody'),
        btnRefreshPriority: document.getElementById('btnRefreshPriority'),

        // Employee Map View
        employeeMapSelect: document.getElementById('employeeMapSelect'),
        employeeTasksBody: document.getElementById('employeeTasksBody'),
        employeeMapTitle: document.getElementById('employeeMapTitle'),
        empMetaDept: document.getElementById('empMetaDept'),
        empMetaEmail: document.getElementById('empMetaEmail'),
        empMetaTaskCount: document.getElementById('empMetaTaskCount'),

        // Edit Task Modal
        editTaskModal: document.getElementById('editTaskModal'),
        editTaskForm: document.getElementById('editTaskForm'),
        editTaskId: document.getElementById('editTaskId'),
        editTaskTitle: document.getElementById('editTaskTitle'),
        editTaskDesc: document.getElementById('editTaskDesc'),
        editTaskEmp: document.getElementById('editTaskEmp'),
        editTaskPriority: document.getElementById('editTaskPriority'),
        editTaskDeadline: document.getElementById('editTaskDeadline'),
        editTaskStatus: document.getElementById('editTaskStatus'),

        // Toast
        toastContainer: document.getElementById('toastContainer')
    };

    // =========================================================================
    // Initializer
    // =========================================================================
    function init() {
        setupEventListeners();
        setDefaultDeadline();
        refreshAllData();
    }

    function setDefaultDeadline() {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 3);
        const yyyy = tomorrow.getFullYear();
        const mm = String(tomorrow.getMonth() + 1).padStart(2, '0');
        const dd = String(tomorrow.getDate()).padStart(2, '0');
        if (elements.taskFormDeadline) {
            elements.taskFormDeadline.value = `${yyyy}-${mm}-${dd}`;
        }
    }

    // =========================================================================
    // Event Listeners
    // =========================================================================
    function setupEventListeners() {
        // Navigation items
        elements.navItems.forEach(item => {
            item.addEventListener('click', () => {
                const view = item.getAttribute('data-view');
                switchView(view);
            });
        });

        // Quick add buttons
        if (elements.btnQuickAdd) {
            elements.btnQuickAdd.addEventListener('click', () => switchView('assignTask'));
        }
        if (elements.btnOpenAssignTask) {
            elements.btnOpenAssignTask.addEventListener('click', () => switchView('assignTask'));
        }
        if (elements.btnCancelAssign) {
            elements.btnCancelAssign.addEventListener('click', () => switchView('tasks'));
        }

        // View All Priority shortcut
        document.querySelectorAll('[data-action="goToPriority"]').forEach(btn => {
            btn.addEventListener('click', () => switchView('priorityTasks'));
        });

        // Sidebar mobile toggle
        if (elements.sidebarToggle) {
            elements.sidebarToggle.addEventListener('click', () => {
                elements.appSidebar.classList.toggle('open');
            });
        }

        // Global search
        if (elements.globalSearch) {
            elements.globalSearch.addEventListener('input', (e) => {
                const q = e.target.value.trim().toLowerCase();
                if (q.length > 0) {
                    if (state.currentView !== 'tasks') {
                        switchView('tasks');
                    }
                    elements.taskSearchInput.value = q;
                    filterTasks();
                }
            });
        }

        // Employee Search
        if (elements.employeeSearchInput) {
            elements.employeeSearchInput.addEventListener('input', () => filterEmployees());
        }

        // Task Filters & Search
        if (elements.taskSearchInput) {
            elements.taskSearchInput.addEventListener('input', () => filterTasks());
        }
        if (elements.taskPriorityFilter) {
            elements.taskPriorityFilter.addEventListener('change', () => filterTasks());
        }
        if (elements.taskStatusFilter) {
            elements.taskStatusFilter.addEventListener('change', () => filterTasks());
        }

        // Priority Queue Refresh
        if (elements.btnRefreshPriority) {
            elements.btnRefreshPriority.addEventListener('click', () => loadPriorityTasks());
        }

        // Employee Map Selector
        if (elements.employeeMapSelect) {
            elements.employeeMapSelect.addEventListener('change', (e) => {
                const empId = parseInt(e.target.value, 10);
                renderEmployeeMappedTasks(empId);
            });
        }

        // Add Employee Modal
        if (elements.btnOpenAddEmployee) {
            elements.btnOpenAddEmployee.addEventListener('click', () => openEmployeeModal());
        }

        // Employee Form Submit
        if (elements.employeeForm) {
            elements.employeeForm.addEventListener('submit', handleEmployeeFormSubmit);
        }

        // Assign Task Form Submit
        if (elements.assignTaskForm) {
            elements.assignTaskForm.addEventListener('submit', handleAssignTaskSubmit);
        }

        // Edit Task Form Submit
        if (elements.editTaskForm) {
            elements.editTaskForm.addEventListener('submit', handleEditTaskSubmit);
        }

        // Modal Close handlers
        document.querySelectorAll('[data-close-modal]').forEach(btn => {
            btn.addEventListener('click', () => {
                elements.employeeModal.classList.remove('open');
                elements.editTaskModal.classList.remove('open');
            });
        });

        // Retry DB connection
        if (elements.btnRetryDb) {
            elements.btnRetryDb.addEventListener('click', () => refreshAllData());
        }
    }

    // =========================================================================
    // View Management
    // =========================================================================
    function switchView(viewName) {
        state.currentView = viewName;

        elements.navItems.forEach(item => {
            if (item.getAttribute('data-view') === viewName) {
                item.classList.add('active');
            } else {
                item.classList.remove('active');
            }
        });

        elements.viewSections.forEach(section => {
            section.classList.remove('active');
        });

        const targetSection = document.getElementById('view' + capitalize(viewName));
        if (targetSection) {
            targetSection.classList.add('active');
        }

        // Close mobile sidebar if open
        if (elements.appSidebar) {
            elements.appSidebar.classList.remove('open');
        }

        // Update headings
        switch (viewName) {
            case 'dashboard':
                elements.pageHeading.textContent = 'Dashboard Overview';
                elements.pageSubheading.textContent = 'Real-time statistics & task monitoring';
                loadDashboardStats();
                break;
            case 'employees':
                elements.pageHeading.textContent = 'Employee Management';
                elements.pageSubheading.textContent = 'Manage team members and view assignments';
                loadEmployees();
                break;
            case 'tasks':
                elements.pageHeading.textContent = 'All Tasks';
                elements.pageSubheading.textContent = 'Browse, filter, and update task statuses';
                loadTasks();
                break;
            case 'assignTask':
                elements.pageHeading.textContent = 'Task Assignment';
                elements.pageSubheading.textContent = 'Assign deliverables to team members';
                populateEmployeeDropdowns();
                break;
            case 'priorityTasks':
                elements.pageHeading.textContent = 'Priority Queue Tasks';
                elements.pageSubheading.textContent = 'Demonstrating Java PriorityQueue<Task> (Min-Heap)';
                loadPriorityTasks();
                break;
            case 'employeeTasks':
                elements.pageHeading.textContent = 'Employee Task Mapping';
                elements.pageSubheading.textContent = 'Demonstrating Java HashMap<Integer, ArrayList<Task>>';
                loadEmployeeMap();
                break;
        }
    }

    function capitalize(str) {
        return str.charAt(0).toUpperCase() + str.slice(1);
    }

    // =========================================================================
    // API Data Fetching & Sync
    // =========================================================================
    async function refreshAllData() {
        await Promise.all([
            loadDashboardStats(),
            loadEmployees(),
            loadTasks()
        ]);
        populateEmployeeDropdowns();
    }

    async function loadDashboardStats() {
        try {
            const res = await fetch('/api/dashboard');
            const data = await res.json();
            if (data.success && data.data) {
                state.stats = data.data;
                updateDashboardUI(data.data);
                updateDbStatusUI(data.data.databaseConnected, data.data.databaseUrl, data.data.databaseError);
            }
        } catch (err) {
            console.error('Error fetching dashboard stats:', err);
            updateDbStatusUI(false, null, err.message);
        }
    }

    function updateDbStatusUI(connected, url, error) {
        if (connected) {
            elements.dbIndicator.className = 'status-indicator online';
            elements.dbStatusLabel.textContent = 'MySQL Connected';
            elements.dbSubtext.textContent = 'employee_task_management';
            elements.dbWarningBanner.classList.add('hidden');
        } else {
            elements.dbIndicator.className = 'status-indicator offline';
            elements.dbStatusLabel.textContent = 'MySQL Disconnected';
            elements.dbSubtext.textContent = 'Check database.properties';
            elements.dbWarningBanner.classList.remove('hidden');
            if (error) {
                elements.dbWarningText.textContent = `MySQL connection issue: ${error}. Please verify your password in database.properties.`;
            }
        }
    }

    function updateDashboardUI(stats) {
        elements.kpiEmployees.textContent = stats.totalEmployees ?? 0;
        elements.kpiTasks.textContent = stats.totalTasks ?? 0;
        elements.kpiPending.textContent = stats.pendingTasks ?? 0;
        elements.kpiInProgress.textContent = stats.inProgressTasks ?? 0;
        elements.kpiCompleted.textContent = stats.completedTasks ?? 0;
        elements.kpiOverdue.textContent = stats.overdueTasks ?? 0;
        elements.kpiCompletionRate.textContent = `${stats.completionRate ?? 0}% completed`;

        elements.navEmpCount.textContent = stats.totalEmployees ?? 0;
        elements.navTaskCount.textContent = stats.totalTasks ?? 0;

        // Render preview table on dashboard from priority queue
        loadDashboardPriorityPreview();
    }

    async function loadDashboardPriorityPreview() {
        try {
            const res = await fetch('/api/tasks?type=priority');
            const data = await res.json();
            if (data.success && Array.isArray(data.data)) {
                renderDashboardPriorityTable(data.data.slice(0, 5));
            }
        } catch (err) {
            console.error('Error loading dashboard priority preview:', err);
        }
    }

    function renderDashboardPriorityTable(tasks) {
        if (!elements.dashboardPriorityBody) return;
        if (!tasks || tasks.length === 0) {
            elements.dashboardPriorityBody.innerHTML = `<tr><td colspan="7" class="text-center text-muted">No pending priority tasks found. All caught up!</td></tr>`;
            return;
        }

        elements.dashboardPriorityBody.innerHTML = tasks.map(task => `
            <tr>
                <td><code>#${task.taskId}</code></td>
                <td>
                    <strong>${escapeHtml(task.taskTitle)}</strong>
                    ${task.description ? `<br><small class="text-muted">${escapeHtml(truncate(task.description, 50))}</small>` : ''}
                </td>
                <td>${escapeHtml(task.employeeName || `Emp #${task.employeeId}`)}</td>
                <td>${renderPriorityBadge(task.priority)}</td>
                <td><code>${task.deadline || 'N/A'}</code></td>
                <td>${renderDeadlineBadge(task.deadlineStatus)}</td>
                <td>
                    <select class="inline-status-select" onchange="window.updateStatus(${task.taskId}, this.value)">
                        <option value="PENDING" ${task.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                        <option value="IN PROGRESS" ${task.status === 'IN PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
                        <option value="COMPLETED" ${task.status === 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                    </select>
                </td>
            </tr>
        `).join('');
    }

    // =========================================================================
    // Employee Management Logic
    // =========================================================================
    async function loadEmployees() {
        try {
            const res = await fetch('/api/employees');
            const data = await res.json();
            if (data.success && Array.isArray(data.data)) {
                state.employees = data.data;
                renderEmployeesTable(state.employees);
                populateEmployeeDropdowns();
            }
        } catch (err) {
            showToast('Failed to load employees: ' + err.message, 'error');
        }
    }

    function renderEmployeesTable(employees) {
        if (!elements.employeesTableBody) return;
        if (!employees || employees.length === 0) {
            elements.employeesTableBody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No employees registered yet. Click "Add Employee" to create one.</td></tr>`;
            return;
        }

        // Count tasks per employee
        const taskCountMap = {};
        state.tasks.forEach(t => {
            taskCountMap[t.employeeId] = (taskCountMap[t.employeeId] || 0) + 1;
        });

        elements.employeesTableBody.innerHTML = employees.map(emp => `
            <tr>
                <td><code>#${emp.employeeId}</code></td>
                <td><strong>${escapeHtml(emp.employeeName)}</strong></td>
                <td><a href="mailto:${escapeHtml(emp.email)}" style="color:#a5b4fc;">${escapeHtml(emp.email)}</a></td>
                <td><span class="badge badge-subtle">${escapeHtml(emp.department)}</span></td>
                <td><span class="badge badge-subtle">${taskCountMap[emp.employeeId] || 0} tasks</span></td>
                <td class="text-right">
                    <button class="btn-icon" title="Edit" onclick="window.openEditEmployee(${emp.employeeId})">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                    </button>
                    <button class="btn-icon danger" title="Delete" onclick="window.confirmDeleteEmployee(${emp.employeeId})">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                    </button>
                </td>
            </tr>
        `).join('');
    }

    function filterEmployees() {
        const q = elements.employeeSearchInput.value.trim().toLowerCase();
        if (!q) {
            renderEmployeesTable(state.employees);
            return;
        }
        const filtered = state.employees.filter(e =>
            String(e.employeeId).includes(q) ||
            e.employeeName.toLowerCase().includes(q) ||
            e.email.toLowerCase().includes(q) ||
            e.department.toLowerCase().includes(q)
        );
        renderEmployeesTable(filtered);
    }

    function openEmployeeModal(emp = null) {
        state.editingEmployee = emp;
        if (emp) {
            elements.employeeModalTitle.textContent = 'Edit Employee';
            elements.empModalId.value = emp.employeeId;
            elements.empModalId.disabled = true;
            elements.empModalName.value = emp.employeeName;
            elements.empModalEmail.value = emp.email;
            elements.empModalDept.value = emp.department;
        } else {
            elements.employeeModalTitle.textContent = 'Add New Employee';
            elements.empModalId.disabled = false;
            // Suggest next available ID
            const maxId = state.employees.reduce((max, e) => Math.max(max, e.employeeId), 100);
            elements.empModalId.value = maxId + 1;
            elements.empModalName.value = '';
            elements.empModalEmail.value = '';
            elements.empModalDept.value = '';
        }
        elements.employeeModal.classList.add('open');
    }

    async function handleEmployeeFormSubmit(e) {
        e.preventDefault();
        const emp = {
            employeeId: parseInt(elements.empModalId.value, 10),
            employeeName: elements.empModalName.value.trim(),
            email: elements.empModalEmail.value.trim(),
            department: elements.empModalDept.value.trim()
        };

        const isEdit = state.editingEmployee !== null;
        const method = isEdit ? 'PUT' : 'POST';

        try {
            const res = await fetch('/api/employees', {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(emp)
            });
            const data = await res.json();
            if (data.success) {
                showToast(isEdit ? 'Employee updated successfully!' : 'Employee added to MySQL!', 'success');
                elements.employeeModal.classList.remove('open');
                await refreshAllData();
            } else {
                showToast(data.error || 'Failed to save employee', 'error');
            }
        } catch (err) {
            showToast('Error saving employee: ' + err.message, 'error');
        }
    }

    window.openEditEmployee = function (empId) {
        const emp = state.employees.find(e => e.employeeId === empId);
        if (emp) openEmployeeModal(emp);
    };

    window.confirmDeleteEmployee = async function (empId) {
        if (!confirm(`Are you sure you want to delete Employee #${empId}? This will also delete all their assigned tasks due to MySQL foreign key cascade.`)) {
            return;
        }
        try {
            const res = await fetch(`/api/employees?id=${empId}`, { method: 'DELETE' });
            const data = await res.json();
            if (data.success) {
                showToast('Employee deleted successfully.', 'success');
                await refreshAllData();
            } else {
                showToast(data.error || 'Failed to delete employee', 'error');
            }
        } catch (err) {
            showToast('Error deleting employee: ' + err.message, 'error');
        }
    };

    // =========================================================================
    // Task Management Logic
    // =========================================================================
    async function loadTasks() {
        try {
            const res = await fetch('/api/tasks');
            const data = await res.json();
            if (data.success && Array.isArray(data.data)) {
                state.tasks = data.data;
                renderAllTasksTable(state.tasks);
            }
        } catch (err) {
            showToast('Failed to load tasks: ' + err.message, 'error');
        }
    }

    function renderAllTasksTable(tasks) {
        if (!elements.allTasksTableBody) return;
        if (!tasks || tasks.length === 0) {
            elements.allTasksTableBody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No tasks found matching your filter criteria.</td></tr>`;
            return;
        }

        elements.allTasksTableBody.innerHTML = tasks.map(task => `
            <tr>
                <td><code>#${task.taskId}</code></td>
                <td>
                    <strong>${escapeHtml(task.taskTitle)}</strong>
                    ${task.description ? `<br><small class="text-muted">${escapeHtml(truncate(task.description, 60))}</small>` : ''}
                </td>
                <td>${escapeHtml(task.employeeName || `Emp #${task.employeeId}`)}</td>
                <td>${renderPriorityBadge(task.priority)}</td>
                <td><code>${task.deadline || 'N/A'}</code></td>
                <td>${renderDeadlineBadge(task.deadlineStatus)}</td>
                <td>
                    <select class="inline-status-select" onchange="window.updateStatus(${task.taskId}, this.value)">
                        <option value="PENDING" ${task.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                        <option value="IN PROGRESS" ${task.status === 'IN PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
                        <option value="COMPLETED" ${task.status === 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                    </select>
                </td>
                <td class="text-right">
                    <button class="btn-icon" title="Edit" onclick="window.openEditTask(${task.taskId})">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                    </button>
                    <button class="btn-icon danger" title="Delete" onclick="window.confirmDeleteTask(${task.taskId})">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                    </button>
                </td>
            </tr>
        `).join('');
    }

    function filterTasks() {
        const q = elements.taskSearchInput ? elements.taskSearchInput.value.trim().toLowerCase() : '';
        const prio = elements.taskPriorityFilter ? elements.taskPriorityFilter.value : 'ALL';
        const stat = elements.taskStatusFilter ? elements.taskStatusFilter.value : 'ALL';

        let filtered = state.tasks;
        if (prio !== 'ALL') {
            filtered = filtered.filter(t => t.priority === prio);
        }
        if (stat !== 'ALL') {
            filtered = filtered.filter(t => t.status === stat);
        }
        if (q) {
            filtered = filtered.filter(t =>
                String(t.taskId).includes(q) ||
                t.taskTitle.toLowerCase().includes(q) ||
                (t.description && t.description.toLowerCase().includes(q)) ||
                (t.employeeName && t.employeeName.toLowerCase().includes(q))
            );
        }
        renderAllTasksTable(filtered);
    }

    function populateEmployeeDropdowns() {
        const optionsHtml = state.employees.map(e =>
            `<option value="${e.employeeId}">${escapeHtml(e.employeeName)} (#${e.employeeId} - ${escapeHtml(e.department)})</option>`
        ).join('');

        if (elements.taskFormEmployee) {
            elements.taskFormEmployee.innerHTML = `<option value="">-- Select Employee --</option>` + optionsHtml;
        }
        if (elements.editTaskEmp) {
            elements.editTaskEmp.innerHTML = `<option value="">-- Select Employee --</option>` + optionsHtml;
        }
        if (elements.employeeMapSelect) {
            const currentSelected = elements.employeeMapSelect.value;
            elements.employeeMapSelect.innerHTML = `<option value="">-- Choose Employee --</option>` + optionsHtml;
            if (currentSelected) {
                elements.employeeMapSelect.value = currentSelected;
            }
        }

        // Auto-suggest next Task ID
        if (elements.taskFormId && !elements.taskFormId.value) {
            const maxTaskId = state.tasks.reduce((max, t) => Math.max(max, t.taskId), 1000);
            elements.taskFormId.value = maxTaskId + 1;
        }
    }

    async function handleAssignTaskSubmit(e) {
        e.preventDefault();

        const task = {
            taskId: parseInt(elements.taskFormId.value, 10),
            taskTitle: elements.taskFormTitle.value.trim(),
            description: elements.taskFormDesc.value.trim(),
            employeeId: parseInt(elements.taskFormEmployee.value, 10),
            priority: elements.taskFormPriority.value,
            deadline: elements.taskFormDeadline.value,
            status: elements.taskFormStatus.value
        };

        if (!task.taskId || !task.taskTitle || !task.employeeId || !task.deadline) {
            showToast('Please fill all required task fields.', 'error');
            return;
        }

        try {
            const res = await fetch('/api/tasks', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(task)
            });
            const data = await res.json();
            if (data.success) {
                showToast(`Task #${task.taskId} created and stored in MySQL!`, 'success');
                elements.assignTaskForm.reset();
                setDefaultDeadline();
                await refreshAllData();
                switchView('tasks');
            } else {
                showToast(data.error || 'Failed to create task', 'error');
            }
        } catch (err) {
            showToast('Error creating task: ' + err.message, 'error');
        }
    }

    window.openEditTask = function (taskId) {
        const task = state.tasks.find(t => t.taskId === taskId);
        if (!task) return;
        state.editingTask = task;

        elements.editTaskId.value = task.taskId;
        elements.editTaskTitle.value = task.taskTitle;
        elements.editTaskDesc.value = task.description || '';
        elements.editTaskEmp.value = task.employeeId;
        elements.editTaskPriority.value = task.priority;
        elements.editTaskDeadline.value = task.deadline || '';
        elements.editTaskStatus.value = task.status;

        elements.editTaskModal.classList.add('open');
    };

    async function handleEditTaskSubmit(e) {
        e.preventDefault();
        const task = {
            taskId: parseInt(elements.editTaskId.value, 10),
            taskTitle: elements.editTaskTitle.value.trim(),
            description: elements.editTaskDesc.value.trim(),
            employeeId: parseInt(elements.editTaskEmp.value, 10),
            priority: elements.editTaskPriority.value,
            deadline: elements.editTaskDeadline.value,
            status: elements.editTaskStatus.value
        };

        try {
            const res = await fetch('/api/tasks', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(task)
            });
            const data = await res.json();
            if (data.success) {
                showToast('Task updated successfully in MySQL!', 'success');
                elements.editTaskModal.classList.remove('open');
                await refreshAllData();
            } else {
                showToast(data.error || 'Failed to update task', 'error');
            }
        } catch (err) {
            showToast('Error updating task: ' + err.message, 'error');
        }
    }

    window.updateStatus = async function (taskId, newStatus) {
        try {
            const res = await fetch('/api/tasks', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    action: 'status',
                    taskId: taskId,
                    status: newStatus
                })
            });
            const data = await res.json();
            if (data.success) {
                showToast(`Task #${taskId} updated to ${newStatus}`, 'success');
                await refreshAllData();
            } else {
                showToast(data.error || 'Failed to update status', 'error');
            }
        } catch (err) {
            showToast('Error updating status: ' + err.message, 'error');
        }
    };

    window.confirmDeleteTask = async function (taskId) {
        if (!confirm(`Are you sure you want to delete Task #${taskId} from MySQL?`)) {
            return;
        }
        try {
            const res = await fetch(`/api/tasks?id=${taskId}`, { method: 'DELETE' });
            const data = await res.json();
            if (data.success) {
                showToast(`Task #${taskId} deleted successfully.`, 'success');
                await refreshAllData();
            } else {
                showToast(data.error || 'Failed to delete task', 'error');
            }
        } catch (err) {
            showToast('Error deleting task: ' + err.message, 'error');
        }
    };

    // =========================================================================
    // PriorityQueue View Logic
    // =========================================================================
    async function loadPriorityTasks() {
        try {
            const res = await fetch('/api/tasks?type=priority');
            const data = await res.json();
            if (data.success && Array.isArray(data.data)) {
                state.priorityTasks = data.data;
                renderPriorityQueueTable(state.priorityTasks);
            }
        } catch (err) {
            showToast('Failed to load PriorityQueue: ' + err.message, 'error');
        }
    }

    function renderPriorityQueueTable(tasks) {
        if (!elements.priorityQueueBody) return;
        if (!tasks || tasks.length === 0) {
            elements.priorityQueueBody.innerHTML = `<tr><td colspan="9" class="text-center text-muted">No pending or in-progress tasks currently in PriorityQueue.</td></tr>`;
            return;
        }

        elements.priorityQueueBody.innerHTML = tasks.map((task, index) => `
            <tr>
                <td><span class="pill-tag" style="background:#4338ca; color:#c7d2fe;">Rank #${index + 1}</span></td>
                <td><code>#${task.taskId}</code></td>
                <td><strong>${escapeHtml(task.taskTitle)}</strong></td>
                <td>${escapeHtml(task.employeeName || `Emp #${task.employeeId}`)}</td>
                <td>${renderPriorityBadge(task.priority)}</td>
                <td><code>${task.deadline || 'N/A'}</code></td>
                <td>${renderDeadlineBadge(task.deadlineStatus)}</td>
                <td><span class="badge ${getStatusBadgeClass(task.status)}">${task.status}</span></td>
                <td class="text-right">
                    <button class="btn btn-sm btn-outline" onclick="window.updateStatus(${task.taskId}, 'COMPLETED')">
                        Mark Done
                    </button>
                </td>
            </tr>
        `).join('');
    }

    // =========================================================================
    // HashMap View Logic
    // =========================================================================
    async function loadEmployeeMap() {
        try {
            const res = await fetch('/api/tasks?type=employee-map');
            const data = await res.json();
            if (data.success && data.data) {
                state.employeeMap = data.data;
                populateEmployeeDropdowns();
                if (elements.employeeMapSelect && elements.employeeMapSelect.value) {
                    renderEmployeeMappedTasks(parseInt(elements.employeeMapSelect.value, 10));
                }
            }
        } catch (err) {
            showToast('Failed to load Employee Tasks Map: ' + err.message, 'error');
        }
    }

    function renderEmployeeMappedTasks(empId) {
        if (!empId) {
            elements.employeeTasksBody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">Select an employee above to view mapped tasks.</td></tr>`;
            elements.empMetaDept.textContent = '-';
            elements.empMetaEmail.textContent = '-';
            elements.empMetaTaskCount.textContent = '0';
            elements.employeeMapTitle.textContent = 'Assigned Tasks';
            return;
        }

        const emp = state.employees.find(e => e.employeeId === empId);
        if (emp) {
            elements.empMetaDept.textContent = emp.department;
            elements.empMetaEmail.textContent = emp.email;
            elements.employeeMapTitle.textContent = `Assigned Tasks for ${emp.employeeName} (#${emp.employeeId})`;
        }

        const tasks = state.employeeMap[empId] || [];
        elements.empMetaTaskCount.textContent = tasks.length;

        if (tasks.length === 0) {
            elements.employeeTasksBody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No tasks currently assigned to this employee.</td></tr>`;
            return;
        }

        elements.employeeTasksBody.innerHTML = tasks.map(task => `
            <tr>
                <td><code>#${task.taskId}</code></td>
                <td><strong>${escapeHtml(task.taskTitle)}</strong></td>
                <td>${renderPriorityBadge(task.priority)}</td>
                <td><code>${task.deadline || 'N/A'}</code></td>
                <td>${renderDeadlineBadge(task.deadlineStatus)}</td>
                <td>
                    <select class="inline-status-select" onchange="window.updateStatus(${task.taskId}, this.value)">
                        <option value="PENDING" ${task.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                        <option value="IN PROGRESS" ${task.status === 'IN PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
                        <option value="COMPLETED" ${task.status === 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                    </select>
                </td>
                <td><code>${task.completionDate || '-'}</code></td>
                <td class="text-right">
                    <button class="btn-icon danger" title="Delete" onclick="window.confirmDeleteTask(${task.taskId})">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                    </button>
                </td>
            </tr>
        `).join('');
    }

    // =========================================================================
    // UI Helpers & Badges
    // =========================================================================
    function renderPriorityBadge(priority) {
        const p = (priority || 'MEDIUM').toUpperCase();
        let cls = 'badge-priority-medium';
        if (p === 'HIGH') cls = 'badge-priority-high';
        if (p === 'LOW') cls = 'badge-priority-low';
        return `<span class="badge ${cls}">${p}</span>`;
    }

    function renderDeadlineBadge(status) {
        const s = (status || 'ON TRACK').toUpperCase();
        let cls = 'badge-deadline-on-track';
        if (s === 'OVERDUE') cls = 'badge-deadline-overdue';
        else if (s === 'DUE SOON') cls = 'badge-deadline-due-soon';
        else if (s === 'COMPLETED') cls = 'badge-deadline-completed';
        return `<span class="badge ${cls}">${s}</span>`;
    }

    function getStatusBadgeClass(status) {
        const s = (status || 'PENDING').toUpperCase();
        if (s === 'COMPLETED') return 'badge-status-completed';
        if (s === 'IN PROGRESS') return 'badge-status-in-progress';
        return 'badge-status-pending';
    }

    function escapeHtml(text) {
        if (!text) return '';
        return String(text)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    function truncate(str, max) {
        if (!str) return '';
        return str.length > max ? str.substring(0, max) + '...' : str;
    }

    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <span>${escapeHtml(message)}</span>
        `;
        elements.toastContainer.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3500);
    }

    // Run
    init();
});
