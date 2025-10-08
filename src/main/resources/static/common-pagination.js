// Common Pagination Utility
// This file provides reusable pagination functionality for tables and lists

class Paginator {
    constructor(items, itemsPerPage = 10) {
        this.allItems = items;
        this.itemsPerPage = itemsPerPage;
        this.currentPage = 1;
        this.totalPages = Math.ceil(items.length / itemsPerPage);
    }

    // Update items and recalculate pagination
    updateItems(items) {
        this.allItems = items;
        this.totalPages = Math.ceil(items.length / this.itemsPerPage);
        // Reset to page 1 if current page exceeds new total
        if (this.currentPage > this.totalPages) {
            this.currentPage = Math.max(1, this.totalPages);
        }
    }

    // Get items for current page
    getCurrentPageItems() {
        const startIndex = (this.currentPage - 1) * this.itemsPerPage;
        const endIndex = startIndex + this.itemsPerPage;
        return this.allItems.slice(startIndex, endIndex);
    }

    // Navigate to specific page
    goToPage(pageNumber) {
        if (pageNumber >= 1 && pageNumber <= this.totalPages) {
            this.currentPage = pageNumber;
            return true;
        }
        return false;
    }

    // Navigate to next page
    nextPage() {
        return this.goToPage(this.currentPage + 1);
    }

    // Navigate to previous page
    previousPage() {
        return this.goToPage(this.currentPage - 1);
    }

    // Get pagination info
    getInfo() {
        const startItem = this.allItems.length === 0 ? 0 : (this.currentPage - 1) * this.itemsPerPage + 1;
        const endItem = Math.min(this.currentPage * this.itemsPerPage, this.allItems.length);
        
        return {
            currentPage: this.currentPage,
            totalPages: this.totalPages,
            totalItems: this.allItems.length,
            startItem: startItem,
            endItem: endItem,
            hasNext: this.currentPage < this.totalPages,
            hasPrevious: this.currentPage > 1
        };
    }

    // Generate pagination HTML
    generatePaginationHTML(containerId) {
        const info = this.getInfo();
        
        if (info.totalPages <= 1) {
            return ''; // No pagination needed for single page
        }

        let html = '<div class="pagination-container">';
        html += '<div class="pagination-info">';
        html += `Mostrando ${info.startItem}-${info.endItem} de ${info.totalItems}`;
        html += '</div>';
        html += '<div class="pagination-controls">';
        
        // Previous button
        html += `<button class="pagination-btn" ${!info.hasPrevious ? 'disabled' : ''} 
                 onclick="window.paginator_${containerId}.previousPage(); window.renderWithPagination_${containerId}()">
                 ← Anterior
                 </button>`;
        
        // Page numbers
        const maxVisiblePages = 5;
        let startPage = Math.max(1, info.currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(info.totalPages, startPage + maxVisiblePages - 1);
        
        // Adjust start if we're near the end
        if (endPage - startPage < maxVisiblePages - 1) {
            startPage = Math.max(1, endPage - maxVisiblePages + 1);
        }
        
        // First page
        if (startPage > 1) {
            html += `<button class="pagination-btn" onclick="window.paginator_${containerId}.goToPage(1); window.renderWithPagination_${containerId}()">1</button>`;
            if (startPage > 2) {
                html += '<span class="pagination-ellipsis">...</span>';
            }
        }
        
        // Page numbers
        for (let i = startPage; i <= endPage; i++) {
            const activeClass = i === info.currentPage ? 'active' : '';
            html += `<button class="pagination-btn ${activeClass}" 
                     onclick="window.paginator_${containerId}.goToPage(${i}); window.renderWithPagination_${containerId}()">${i}</button>`;
        }
        
        // Last page
        if (endPage < info.totalPages) {
            if (endPage < info.totalPages - 1) {
                html += '<span class="pagination-ellipsis">...</span>';
            }
            html += `<button class="pagination-btn" onclick="window.paginator_${containerId}.goToPage(${info.totalPages}); window.renderWithPagination_${containerId}()">${info.totalPages}</button>`;
        }
        
        // Next button
        html += `<button class="pagination-btn" ${!info.hasNext ? 'disabled' : ''} 
                 onclick="window.paginator_${containerId}.nextPage(); window.renderWithPagination_${containerId}()">
                 Siguiente →
                 </button>`;
        
        html += '</div></div>';
        
        return html;
    }
}

// Export for use in other scripts
window.Paginator = Paginator;