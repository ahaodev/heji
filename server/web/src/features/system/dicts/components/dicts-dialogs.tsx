import { ItemsDialogs } from './items-dialogs'
import { ItemsListDialog } from './items-list-dialog'
import { TypesDialogs } from './types-dialogs'

export function DictsDialogs() {
  return (
    <>
      <TypesDialogs />
      <ItemsDialogs />
      <ItemsListDialog />
    </>
  )
}
