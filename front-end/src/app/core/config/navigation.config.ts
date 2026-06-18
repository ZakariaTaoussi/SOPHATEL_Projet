import { Role } from '../enums/role.enum';
import { NavItem } from '../models/nav-item.model';

const ALL = [
  Role.EMPLOYE,
  Role.RH,
  Role.RESPONSABLE,
  Role.ADMINISTRATEUR,
  Role.DIRECTEUR_GENERAL,
];

export const NAV_ITEMS: NavItem[] = [
  {
    label: 'Tableau de bord',
    icon: 'LayoutDashboard',
    route: '/user/dashboard',
    roles: ALL
  },

  {
    label: 'Mes demandes',
    icon: 'FileText',
    route: '/user/demandes',
    roles: [Role.EMPLOYE, Role.RH, Role.RESPONSABLE]
  },

  {
    label: 'Mes absences',
    icon: 'CalendarDays',
    route: '/user/absences',
    roles: [Role.EMPLOYE, Role.RH, Role.RESPONSABLE]
  },

  {
    label: 'Validations',
    icon: 'CheckSquare',
    route: '/validations',
    roles: [Role.RESPONSABLE, Role.DIRECTEUR_GENERAL, Role.RH]
  },

  {
    label: 'Employés',
    icon: 'Users',
    route: '/admin/employes',
    roles: [Role.RH, Role.ADMINISTRATEUR]
  },
  {
    label: 'Départements',
    icon: 'Building2',
    route: '/admin/departements',
    roles: [Role.ADMINISTRATEUR]
  },

  {
    label: 'Jours fériés',
    icon: 'CalendarX',
    route: '/admin/jour-ferie',
    roles: [Role.ADMINISTRATEUR]
  },


  {
    label: 'Historique',
    icon: 'History',
    route: '/user/historique',
    roles: ALL
  },

  {
    label: 'Mon profil',
    icon: 'User',
    route: '/user/profil',
    roles: [Role.EMPLOYE, Role.RH, Role.RESPONSABLE, Role.DIRECTEUR_GENERAL]
  },
];
