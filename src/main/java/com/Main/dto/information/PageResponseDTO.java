package com.Main.dto.information;

import java.util.List;

public class PageResponseDTO<T> {
    private int totalItems;    // 总记录数
    private int totalPages;    // 总页数
    private int currentPage;   // 当前页码
    private List<T> items;     // 当前页数据
    
    public PageResponseDTO() {
    }
    
    public PageResponseDTO(int totalItems, int totalPages, int currentPage, List<T> items) {
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.items = items;
    }
    
    public int getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public List<T> getItems() {
        return items;
    }
    
    public void setItems(List<T> items) {
        this.items = items;
    }
} 