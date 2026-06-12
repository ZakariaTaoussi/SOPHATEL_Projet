import { Role } from '../enums/role.enum';

export interface NavItem {
  label: string;
  icon: string;     // clé d'icône lucide
  route: string;
  roles: Role[];    // rôles autorisés à voir l'item
}
