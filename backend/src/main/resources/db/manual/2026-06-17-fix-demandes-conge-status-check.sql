ALTER TABLE demandes_conge
DROP CONSTRAINT IF EXISTS demandes_conge_status_check;

ALTER TABLE demandes_conge
ADD CONSTRAINT demandes_conge_status_check
CHECK (
    status IN (
        'BROUILLON',
        'VALIDE_EMPLOYE',
        'VALIDE_RESPONSABLE',
        'VALIDE_DG',
        'MODIFICATION_EMPLOYE',
        'MODIFICATION_RESPONSABLE',
        'MODIFICATION_DG',
        'ANNULE',
        'REFUSE_RESPONSABLE',
        'REFUSE_DG'
    )
);
