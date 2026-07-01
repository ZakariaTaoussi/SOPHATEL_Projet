export interface DashboardChartItem {
  label: string;
  value: number;
}

export interface DashboardNotification {
  id: number;
  title: string;
  message: string;
  read: boolean;
  targetUrl?: string | null;
  createdAt: string;
}

export interface DashboardRecentDemande {
  id: number;
  reference: string;
  typeDemande: 'CONGE' | 'ABSENCE' | string;
  status: string;
  employeNomComplet: string;
  departementNom: string;
  dateDebut: string;
  dateFin: string;
  joursDeduits: number;
  createdAt: string;
}

export interface DashboardRecentEmploye {
  id: number;
  matricule: string;
  nomComplet: string;
  email: string;
  role: string;
  departementNom: string;
  createdAt: string;
}

export interface AdminDashboardResponse {
  totalEmployees: number;
  totalEmployesRole: number;
  totalResponsables: number;
  totalRh: number;
  totalDirecteursGeneraux: number;
  totalAdmins: number;
  totalDepartments: number;
  holidaysThisYear: number;
  agendasCount: number;
  currentYearAgendaExists: boolean;
  recentlyCreatedEmployees: DashboardRecentEmploye[];
  employeesByRole: DashboardChartItem[];
  employeesByDepartment: DashboardChartItem[];
}

export interface EmployeDashboardResponse {
  soldeActuel: number;
  soldeTotal: number;
  anneeSolde: number;
  totalDemandesConge: number;
  totalAbsences: number;
  brouillonsCount: number;
  demandesEnAttenteResponsable: number;
  demandesValideesDg: number;
  demandesRefusees: number;
  absencesThisYear: number;
  absencesThisMonth: number;
  latestDemandes: DashboardRecentDemande[];
  latestNotifications: DashboardNotification[];
  unreadNotificationsCount: number;
}

export interface ResponsableDashboardResponse {
  soldeActuel: number;
  soldeTotal: number;
  anneeSolde: number;
  teamMembersCount: number;
  pendingTeamConges: number;
  pendingTeamAbsences: number;
  processedByResponsableThisMonth: number;
  refusedByResponsableThisMonth: number;
  validatedByDgForTeamThisMonth: number;
  teamRequestsByStatus: DashboardChartItem[];
  upcomingApprovedLeaves: DashboardRecentDemande[];
  latestTeamRequests: DashboardRecentDemande[];
  latestNotifications: DashboardNotification[];
  unreadNotificationsCount: number;
}

export interface RhDashboardResponse {
  soldeActuel: number;
  soldeTotal: number;
  anneeSolde: number;
  congesValidesDgThisMonth: number;
  congesValidesDgThisYear: number;
  absencesValideesDgThisMonth: number;
  absencesValideesDgThisYear: number;
  totalJoursCongeValidesThisMonth: number;
  totalJoursAbsencePaieThisMonth: number;
  employeesWithMostAbsences: DashboardChartItem[];
  validatedByDepartment: DashboardChartItem[];
  recentCongesValidesDg: DashboardRecentDemande[];
  recentAbsencesValideesDg: DashboardRecentDemande[];
  latestNotifications: DashboardNotification[];
  unreadNotificationsCount: number;
}

export interface DirecteurGeneralDashboardResponse {
  pendingCongesForDg: number;
  pendingAbsencesForDg: number;
  validatedByDgThisMonth: number;
  refusedByDgThisMonth: number;
  modifiedByDgThisMonth: number;
  totalEmployeesExceptAdmins: number;
  employeesByDepartment: DashboardChartItem[];
  employeesByRole: DashboardChartItem[];
  recentResponsableValidatedRequests: DashboardRecentDemande[];
  recentDgProcessedRequests: DashboardRecentDemande[];
  latestNotifications: DashboardNotification[];
  unreadNotificationsCount: number;
}
