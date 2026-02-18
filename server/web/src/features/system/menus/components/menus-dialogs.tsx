import {MenusCreateDialog} from './menus-create-dialog'
import {MenusDeleteDialog} from './menus-delete-dialog'
import {useMenus} from './menus-provider'

export function MenusDialogs() {
    const {
        showCreateDialog,
        setShowCreateDialog,
        showEditDialog,
        setShowEditDialog,
        showDeleteDialog,
        setShowDeleteDialog,
        currentRow,
    } = useMenus()
    console.log("currentRow")
    console.log(currentRow)
    return (
        <>
            <MenusCreateDialog
                open={showCreateDialog}
                onOpenChange={setShowCreateDialog}
                menu={currentRow}
                mode="create"
            />

            <MenusCreateDialog
                open={showEditDialog}
                onOpenChange={setShowEditDialog}
                menu={currentRow}
                mode="edit"
            />

            <MenusDeleteDialog
                open={showDeleteDialog}
                onOpenChange={setShowDeleteDialog}
                menu={currentRow}
            />
        </>
    )
}