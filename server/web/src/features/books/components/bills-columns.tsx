import { type ColumnDef } from '@tanstack/react-table'
import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { DataTableColumnHeader } from '@/components/data-table'
import { LongText } from '@/components/long-text'
import { type Bill } from '../data/schema'

export const billsColumns: ColumnDef<Bill>[] = [
  {
    accessorKey: 'time',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='日期' />
    ),
    cell: ({ row }) => (
      <div className='w-fit text-nowrap'>{row.getValue('time')}</div>
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
    cell: ({ row }) => {
      const type = row.getValue('type') as number
      return (
        <Badge
          variant='outline'
          className={cn(
            type === 1
              ? 'bg-green-100 text-green-800 border-green-200'
              : 'bg-red-100 text-red-800 border-red-200'
          )}
        >
          {type === 1 ? '收入' : '支出'}
        </Badge>
      )
    },
    filterFn: (row, id, value) => {
      return value.includes(String(row.getValue(id)))
    },
    enableSorting: false,
  },
  {
    accessorKey: 'category',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='分类' />
    ),
    cell: ({ row }) => (
      <div>{row.getValue('category') || '-'}</div>
    ),
    enableSorting: false,
  },
  {
    accessorKey: 'money',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='金额' />
    ),
    cell: ({ row }) => {
      const money = (row.getValue('money') as number) / 100
      const type = row.original.type
      return (
        <div
          className={cn(
            'font-medium text-nowrap',
            type === 1 ? 'text-green-600' : 'text-red-600'
          )}
        >
          {type === 1 ? '+' : '-'}
          {money.toFixed(2)}
        </div>
      )
    },
  },
  {
    accessorKey: 'remark',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='备注' />
    ),
    cell: ({ row }) => (
      <LongText className='max-w-48'>
        {row.getValue('remark') || '-'}
      </LongText>
    ),
    enableSorting: false,
  },
  {
    accessorKey: 'crt_user_name',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='创建人' />
    ),
    cell: ({ row }) => (
      <div className='w-fit text-nowrap'>{row.getValue('crt_user_name') || row.original.crt_user}</div>
    ),
    enableSorting: false,
  },
]
