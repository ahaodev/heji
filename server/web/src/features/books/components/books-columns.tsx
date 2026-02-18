import { type ColumnDef } from '@tanstack/react-table'
import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { DataTableColumnHeader } from '@/components/data-table'
import { LongText } from '@/components/long-text'
import { type Book } from '../data/schema'

export const booksColumns: ColumnDef<Book>[] = [
  {
    accessorKey: 'name',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='账本名称' />
    ),
    cell: ({ row }) => (
      <LongText className='max-w-36'>{row.getValue('name')}</LongText>
    ),
    meta: {
      className: cn(
        'drop-shadow-[0_1px_2px_rgb(0_0_0_/_0.1)] dark:drop-shadow-[0_1px_2px_rgb(255_255_255_/_0.1)]',
        'sticky start-0 @4xl/content:table-cell @4xl/content:drop-shadow-none'
      ),
    },
    enableHiding: false,
  },
  {
    accessorKey: 'type',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='类型' />
    ),
    cell: ({ row }) => (
      <div className='w-fit text-nowrap'>{row.getValue('type') || '-'}</div>
    ),
    enableSorting: false,
  },
  {
    accessorKey: 'is_initial',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='默认' />
    ),
    cell: ({ row }) => {
      const isInitial = row.getValue('is_initial') as boolean
      return isInitial ? (
        <Badge variant='secondary'>默认</Badge>
      ) : (
        <span className='text-muted-foreground'>-</span>
      )
    },
    enableSorting: false,
  },
  {
    accessorKey: 'members',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='成员数' />
    ),
    cell: ({ row }) => {
      const members = row.getValue('members') as string[] | undefined
      return <div>{members?.length ?? 0}</div>
    },
    enableSorting: false,
  },
  {
    accessorKey: 'crt_time',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='创建时间' />
    ),
    cell: ({ row }) => (
      <div className='w-fit text-nowrap'>{row.getValue('crt_time')}</div>
    ),
  },
]
