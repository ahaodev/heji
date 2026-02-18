import {useMemo, useState} from 'react'
import type {SortingState} from '@tanstack/react-table'
import type {TableMenuItem} from '@/lib/menu-utils'

export function useMenuTableState(tableData: TableMenuItem[]) {
    const [expanded, setExpanded] = useState<Record<string, boolean>>({})
    const [sorting, setSorting] = useState<SortingState>([])
    const [searchTerm, setSearchTerm] = useState('')

    const filteredData = useMemo(() => {
        if (!searchTerm) return tableData

        return tableData.filter(item => {
            const matchesSearch = item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                (item.path || '').toLowerCase().includes(searchTerm.toLowerCase())
            return matchesSearch
        })
    }, [tableData, searchTerm])

    const handleExpandToggle = (rowIndex: string) => {
        setExpanded(prev => ({
            ...prev,
            [rowIndex]: !prev[rowIndex]
        }))
    }

    return {
        expanded,
        sorting,
        setSorting,
        searchTerm,
        setSearchTerm,
        filteredData,
        handleExpandToggle
    }
}