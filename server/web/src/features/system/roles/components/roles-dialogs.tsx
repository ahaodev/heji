import {CreateRoleDialog} from './create-role-dialog'
import {RolesDeleteDialog} from './roles-delete-dialog'
import {useRoles} from './roles-provider'

export function RolesDialogs() {
    const {
        showCreateDialog,
        setShowCreateDialog,
        showEditDialog,
        setShowEditDialog,
        showDeleteDialog,
        setShowDeleteDialog,
        currentRow,
    } = useRoles()

    return (
        <>
            <CreateRoleDialog
                open={showCreateDialog}
                onOpenChange={setShowCreateDialog}
            />

            <CreateRoleDialog
                open={showEditDialog}
                onOpenChange={setShowEditDialog}
                role={currentRow}
            />

            <RolesDeleteDialog
                open={showDeleteDialog}
                onOpenChange={setShowDeleteDialog}
                roleAssignment={currentRow}
            />
        </>
    )
}