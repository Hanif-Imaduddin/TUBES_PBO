/**
 * Admin Dashboard - Common JavaScript Functions
 * Dikoding Muda Nusantara
 */

// ============================================
// Sidebar Functions
// ============================================
console.log("Masuk ke admin-commons.js");
function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('show');
    document.getElementById('sidebarOverlay').classList.toggle('show');
}

// ============================================
// Notification Panel
// ============================================
function toggleNotifications() {
    document.getElementById('notificationPanel').classList.toggle('show');
}

// ============================================
// Modal Functions
// ============================================
function openModal(modalId) {
    document.getElementById(modalId).classList.add('show');
    document.body.style.overflow = 'hidden';
    console.log("Membuka modal "+modalId);
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('show');
    document.body.style.overflow = '';
}

// Close modals on overlay click
document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', function(e) {
        if (e.target === this) {
            this.classList.remove('show');
            document.body.style.overflow = '';
        }
    });
});

// Close modals on Escape key
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal-overlay.show').forEach(modal => {
            modal.classList.remove('show');
        });
        document.body.style.overflow = '';
    }
});

// ============================================
// Table Filter Functions
// ============================================
function filterTable(tableId, searchText) {
    const rows = document.querySelectorAll('#' + tableId + ' tbody tr');
    const search = searchText.toLowerCase();
    
    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(search) ? '' : 'none';
    });
}

function filterByStatus(tableId, status) {
    const rows = document.querySelectorAll('#' + tableId + ' tbody tr');
    
    rows.forEach(row => {
        const rowStatus = row.getAttribute('data-status');
        row.style.display = !status || rowStatus === status ? '' : 'none';
    });
}

function filterByRole(tableId, role) {
    const rows = document.querySelectorAll('#' + tableId + ' tbody tr');
    
    rows.forEach(row => {
        const rowRole = row.getAttribute('data-role');
        row.style.display = !role || rowRole === role ? '' : 'none';
    });
}

// ============================================
// Toast Notification
// ============================================
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;
    
    const toast = document.createElement('div');
    toast.className = 'toast ' + type;
    toast.textContent = message;
    container.appendChild(toast);
    
    // Auto remove after 3 seconds
    setTimeout(() => { 
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ============================================
// Delete Functions
// ============================================
let deleteType = '';
let deleteId = '';

function deleteItem(type, id) {
    deleteType = type;
    deleteId = id;
    
    const messages = { 
        course: 'kursus', 
        category: 'kategori', 
        user: 'pengguna' 
    };
    
    document.getElementById('deleteMessage').textContent = 
        'Apakah Anda yakin ingin menghapus ' + (messages[type] || 'item') + ' ini?';
    openModal('deleteModal');
}

function confirmDelete() {
    fetch('/api/admin/' + deleteType + 's/' + deleteId, { 
        method: 'DELETE' 
    })
    .then(response => { 
        if (response.ok) { 
            showToast('Berhasil dihapus', 'success'); 
            closeModal('deleteModal'); 
            setTimeout(() => location.reload(), 1000);
        } else {
            showToast('Gagal menghapus', 'error'); 
        }
    })
    .catch(error => {
        showToast('Terjadi kesalahan', 'error');
    });
}

// ============================================
// Notification Panel - Close on outside click
// ============================================
document.addEventListener('click', function(e) {
    const panel = document.getElementById('notificationPanel');
    if (panel && panel.classList.contains('show')) {
        if (!panel.contains(e.target) && !e.target.closest('.header-icon-btn')) {
            panel.classList.remove('show');
        }
    }
});

// ============================================
// Initialize on DOM Load
// ============================================
document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin Dashboard initialized');
    
    // Handle responsive sidebar
    if (window.innerWidth < 992) {
        document.getElementById('sidebar')?.classList.remove('show');
    }
});

// ============================================
// Utility Functions
// ============================================
function formatCurrency(amount) {
    return 'Rp ' + new Intl.NumberFormat('id-ID').format(amount);
}

function formatDate(dateString) {
    const options = { day: '2-digit', month: 'short', year: 'numeric' };
    return new Date(dateString).toLocaleDateString('id-ID', options);
}

function formatDateTime(dateString) {
    const options = { 
        day: '2-digit', 
        month: 'short', 
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    return new Date(dateString).toLocaleDateString('id-ID', options);
}
