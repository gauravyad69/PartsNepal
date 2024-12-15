export interface ApiResponse<T> {
  data: T;
  message?: string;
  metadata?: {
    page?: number;
    totalPages?: number;
    totalItems?: number;
    itemsPerPage?: number;
  };
} 