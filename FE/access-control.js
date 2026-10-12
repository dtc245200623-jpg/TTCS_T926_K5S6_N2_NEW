/* UI access hints mirror backend AccessPolicy; the API remains authoritative. */
window.AccessControl = (() => {
  const grants = {
    ROLE_ADMIN: [
      'VIEW_JOBS', 'APPLY_FOR_JOB', 'MANAGE_JOBS', 'VIEW_CANDIDATES',
      'MANAGE_CANDIDATES', 'VIEW_APPLICATIONS', 'CREATE_APPLICATION',
      'MANAGE_PIPELINE', 'SCHEDULE_INTERVIEWS', 'MANAGE_ONBOARDING',
      'VIEW_EVALUATIONS', 'SUBMIT_EVALUATIONS', 'MANAGE_ACCOUNTS',
      'APPROVE_OFFERS', 'VIEW_REPORTS', 'MANAGE_CATALOGS',
      'VIEW_REQUISITIONS', 'MANAGE_REQUISITIONS', 'APPROVE_REQUISITIONS',
      'MANAGE_NOTIFICATIONS'
    ],
    ROLE_HR_MANAGER: [
      'VIEW_JOBS', 'APPLY_FOR_JOB', 'MANAGE_JOBS', 'VIEW_CANDIDATES',
      'MANAGE_CANDIDATES', 'VIEW_APPLICATIONS', 'CREATE_APPLICATION',
      'MANAGE_PIPELINE', 'SCHEDULE_INTERVIEWS', 'MANAGE_ONBOARDING',
      'VIEW_EVALUATIONS', 'SUBMIT_EVALUATIONS', 'APPROVE_OFFERS',
      'VIEW_REPORTS', 'MANAGE_CATALOGS', 'VIEW_REQUISITIONS',
      'MANAGE_REQUISITIONS', 'APPROVE_REQUISITIONS', 'MANAGE_NOTIFICATIONS'
    ],
    ROLE_RECRUITER: [
      'VIEW_JOBS', 'APPLY_FOR_JOB', 'MANAGE_JOBS', 'VIEW_CANDIDATES',
      'MANAGE_CANDIDATES', 'VIEW_APPLICATIONS', 'CREATE_APPLICATION',
      'MANAGE_PIPELINE', 'SCHEDULE_INTERVIEWS', 'VIEW_EVALUATIONS',
      'VIEW_REQUISITIONS', 'MANAGE_NOTIFICATIONS'
    ],
    ROLE_HIRING_MANAGER: [
      'VIEW_JOBS', 'APPLY_FOR_JOB', 'MANAGE_JOBS', 'VIEW_CANDIDATES',
      'VIEW_APPLICATIONS', 'MANAGE_PIPELINE', 'VIEW_EVALUATIONS',
      'SUBMIT_EVALUATIONS', 'VIEW_REQUISITIONS', 'MANAGE_REQUISITIONS'
    ],
    ROLE_INTERVIEWER: [
      'VIEW_JOBS', 'APPLY_FOR_JOB', 'VIEW_CANDIDATES',
      'VIEW_APPLICATIONS', 'VIEW_EVALUATIONS', 'SUBMIT_EVALUATIONS'
    ],
    ROLE_APPROVER: [
      'VIEW_JOBS', 'APPROVE_OFFERS', 'VIEW_REQUISITIONS', 'APPROVE_REQUISITIONS'
    ]
  };

  const navigationPermissions = {
    'processing.html': 'VIEW_APPLICATIONS',
    'onboarding.html': 'MANAGE_ONBOARDING',
    'evaluation.html': 'VIEW_EVALUATIONS',
    'employee.html': 'APPLY_FOR_JOB'
  };

  const getStoredRole = () => {
    const user = window.SessionManager?.getUser();
    const value = typeof user?.role === 'string' ? user.role : '';
    return Object.hasOwn(grants, value) ? value : 'ROLE_USER';
  };

  let role = getStoredRole();

  const applyNavigation = () => {
    document.querySelectorAll('a[href]').forEach((link) => {
      let page;
      try {
        page = new URL(link.href, window.location.href).pathname.split('/').pop();
      } catch (_) {
        return;
      }
      const permission = navigationPermissions[page];
      if (permission) link.hidden = !grants[role]?.includes(permission);
    });
  };

  return {
    setRole(value) {
      role = Object.hasOwn(grants, value) ? value : 'ROLE_USER';
      applyNavigation();
    },
    getRole() {
      return role;
    },
    can(permission) {
      return grants[role]?.includes(permission) ?? false;
    },
    async loadRole() {
      if (!window.SessionManager?.isLoggedIn()) {
        role = 'ROLE_USER';
        applyNavigation();
        return role;
      }

      try {
        const response = await window.SessionManager.fetchWithAuth(
          `${window.location.origin}/api/auth/me`
        );
        const result = await response.json();
        if (response.ok && result.success && result.data?.role) {
          this.setRole(result.data.role);
          return role;
        }
      } catch (_) {
        // The protected API remains authoritative if role lookup fails.
      }

      role = getStoredRole();
      applyNavigation();
      return role;
    },
    deny(container, message = 'Vai trò hiện tại không có quyền truy cập chức năng này.') {
      if (!container) return;
      const notice = document.createElement('p');
      notice.className = 'empty-state';
      notice.textContent = message;
      container.replaceChildren(notice);
    }
  };
})();
