import {Cross2Icon} from '@radix-ui/react-icons'
import {type Table} from '@tanstack/react-table'
import {startTransition, useEffect, useState} from 'react'
import {Button} from '@/components/ui/button'
import {Input} from '@/components/ui/input'
import {DataTableFacetedFilter} from './faceted-filter'
import {DataTableViewOptions} from './view-options'
import {useDebounce} from '@/hooks/use-debounce'

type DataTableToolbarProps<TData> = {
    table: Table<TData>
    searchPlaceholder?: string
    searchKey?: string
    filters?: {
        columnId: string
        title: string
        options: {
            label: string
            value: string
            icon?: React.ComponentType<{ className?: string }>
        }[]
        singleSelect?: boolean
    }[]
    onReset?: () => void
}

export function DataTableToolbar<TData>({
                                            table,
                                            searchPlaceholder = 'Filter...',
                                            searchKey,
                                            filters = [],
                                            onReset,
                                        }: DataTableToolbarProps<TData>) {
    // Local state for search input
    const [searchValue, setSearchValue] = useState(() => {
        if (searchKey) {
            return (table.getColumn(searchKey)?.getFilterValue() as string) ?? ''
        }
        return table.getState().globalFilter ?? ''
    })

    // Debounce search value
    const debouncedSearchValue = useDebounce(searchValue, 300)

    // Apply debounced search value to table
    useEffect(() => {
        startTransition(() => {
            if (searchKey) {
                table.getColumn(searchKey)?.setFilterValue(debouncedSearchValue)
            } else {
                table.setGlobalFilter(debouncedSearchValue)
            }
        })
    }, [debouncedSearchValue, searchKey, table])

    const isFiltered =
        table.getState().columnFilters.length > 0 || table.getState().globalFilter

    return (
        <div className='flex items-center justify-between'>
            <div className='flex flex-1 flex-col-reverse items-start gap-y-2 sm:flex-row sm:items-center sm:space-x-2'>
                {searchKey && (
                    <Input
                        placeholder={searchPlaceholder}
                        value={searchValue}
                        onChange={(event) => setSearchValue(event.target.value)}
                        className='h-8 w-[150px] lg:w-[250px]'
                    />
                )}
                <div className='flex gap-x-2'>
                    {filters.map((filter) => {
                        const column = table.getColumn(filter.columnId)
                        if (!column) return null
                        return (
                            <DataTableFacetedFilter
                                key={filter.columnId}
                                column={column}
                                title={filter.title}
                                options={filter.options}
                                singleSelect={filter.singleSelect}
                            />
                        )
                    })}
                </div>
                {isFiltered && (
                    <Button
                        variant='ghost'
                        onClick={() => {
                            setSearchValue('')
                            table.resetColumnFilters()
                            table.setGlobalFilter('')
                            onReset?.()
                        }}
                        className='h-8 px-2 lg:px-3'
                    >
                        重置
                        <Cross2Icon className='ms-2 h-4 w-4'/>
                    </Button>
                )}
            </div>
            <DataTableViewOptions table={table}/>
        </div>
    )
}
