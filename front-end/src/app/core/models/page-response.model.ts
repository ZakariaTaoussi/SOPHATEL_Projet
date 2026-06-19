export interface PageResponse<T> {
  content: T[];
  page?: number;
  currentPage: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first?: boolean;
  last?: boolean;
  empty?: boolean;
}
