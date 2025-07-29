// ===== 4. FRONTEND JAVASCRIPT TO HANDLE ROLE-BASED UI =====
// File: static/js/role-permissions.js
class RolePermissionManager {
    constructor() {
        this.userPermissions = null;
        this.init();
    }

    async init() {
        await this.loadUserPermissions();
        this.updateUI();
    }

    async loadUserPermissions() {
        try {
            const response = await fetch('/api/studies/permissions');
            if (response.ok) {
                this.userPermissions = await response.json();
                console.log('User permissions loaded:', this.userPermissions);
            } else {
                console.error('Failed to load user permissions');
                this.userPermissions = {
                    permissions: {
                        read: false,
                        create: false,
                        update: false,
                        delete: false,
                        import: false,
                        export: false
                    }
                };
            }
        } catch (error) {
            console.error('Error loading user permissions:', error);
        }
    }

    canDelete() {
        return this.userPermissions?.permissions?.delete || false;
    }

    canCreate() {
        return this.userPermissions?.permissions?.create || false;
    }

    canUpdate() {
        return this.userPermissions?.permissions?.update || false;
    }

    canRead() {
        return this.userPermissions?.permissions?.read || false;
    }

    isAdmin() {
        return this.userPermissions?.isAdmin || false;
    }

    updateUI() {
        // Hide/show delete buttons based on permissions
        const deleteButtons = document.querySelectorAll('.delete-btn, [data-action="delete"]');
        deleteButtons.forEach(btn => {
            if (!this.canDelete()) {
                btn.style.display = 'none';
                btn.disabled = true;
            }
        });

        // Hide/show admin-only features
        const adminElements = document.querySelectorAll('.admin-only');
        adminElements.forEach(element => {
            if (!this.isAdmin()) {
                element.style.display = 'none';
            }
        });

        // Update user role display
        const roleDisplay = document.getElementById('user-role');
        if (roleDisplay) {
            roleDisplay.textContent = this.userPermissions?.role || 'Unknown';
        }

        // Update permissions display
        const permissionsDisplay = document.getElementById('user-permissions');
        if (permissionsDisplay && this.userPermissions) {
            const permissions = this.userPermissions.permissions;
            const permissionsList = Object.entries(permissions)
                .filter(([key, value]) => value)
                .map(([key]) => key.toUpperCase())
                .join(', ');
            permissionsDisplay.textContent = permissionsList;
        }
    }

    // Method to check permissions before API calls
    async performDelete(entityType, entityId, confirmMessage = 'Are you sure you want to delete this item?') {
        if (!this.canDelete()) {
            alert('You do not have permission to delete items. Contact an administrator.');
            return false;
        }

        if (!confirm(confirmMessage)) {
            return false;
        }

        try {
            const response = await fetch(`/api/${entityType}/${entityId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const result = await response.json();
                alert(result.message || 'Item deleted successfully');
                return true;
            } else if (response.status === 403) {
                alert('You do not have permission to delete this item.');
                return false;
            } else {
                const error = await response.json();
                alert('Error: ' + (error.error || 'Failed to delete item'));
                return false;
            }
        } catch (error) {
            console.error('Delete error:', error);
            alert('Error deleting item: ' + error.message);
            return false;
        }
    }
}

// Initialize role permission manager when page loads
const roleManager = new RolePermissionManager();