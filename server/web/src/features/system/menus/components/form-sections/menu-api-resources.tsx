import {useEffect, useState} from 'react'
import type {UseFormReturn} from 'react-hook-form'
import {Badge} from '@/components/ui/badge'
import {Button} from '@/components/ui/button'
import {X} from 'lucide-react'
import type {CreateMenuFormData} from '../../schemas/menu-form-schema'
import {useApiResources} from '@/features/system/api-resources/hooks/use-api-resources'
import type {ApiResource} from '@/types/api-resource'
import {METHOD_COLORS} from '@/features/system/api-resources/constants/api-constants'
import {ApiResourceSelector} from './api-resource-selector'
import {useMenus} from '../menus-provider'

interface MenuApiResourcesProps {
    form: UseFormReturn<CreateMenuFormData>
    isEditMode?: boolean
}

export function MenuApiResources({form, isEditMode = false}: MenuApiResourcesProps) {
    const {currentRow} = useMenus()
    // 独立于"权限标识"的 API 资源选择状态，但会同步到表单字段 apiResources
    const [selectedPermissions, setSelectedPermissions] = useState<string[]>([])

    // 同步表单字段和currentRow -> 本地选择（用于编辑模式初始化）
    useEffect(() => {
        const formApiResources = form.getValues('apiResources')

        // 只在编辑模式下才从currentRow获取API资源，创建模式下保持空数组
        const currentRowApiResources = isEditMode ? currentRow?.apiResources : []

        // 优先使用表单数据，如果没有则使用currentRow数据
        const existingResources = formApiResources?.length ? formApiResources : currentRowApiResources

        if (existingResources && existingResources.length > 0) {
            setSelectedPermissions(existingResources)
        } else if (!isEditMode) {
            // 创建模式下确保清空选择
            setSelectedPermissions([])
        }
    }, [form, currentRow, isEditMode])

    // 同步本地选择 -> 表单字段（保证提交时带上）
    useEffect(() => {
        form.setValue('apiResources', selectedPermissions, {shouldDirty: false, shouldValidate: false})
    }, [selectedPermissions, form])

    // Fetch API resources from backend
    const {data: apiResourcesResult, isLoading} = useApiResources({
        page: 1,
        page_size: 1000,
    })
    const apiResources = apiResourcesResult?.data || []

    const handlePermissionToggle = (permissionId: string, checked: boolean) => {
        setSelectedPermissions(prev => {
            if (checked) {
                return prev.includes(permissionId) ? prev : [...prev, permissionId]
            }
            return prev.filter(id => id !== permissionId)
        })
    }

    const handleResourceSelect = (resource: ApiResource, checked: boolean) => {
        // Use method:path format to match backend expectations
        const resourceIdentifier = `${resource.method}:${resource.path}`
        handlePermissionToggle(resourceIdentifier, checked)
    }

    const isResourceSelected = (resource: ApiResource) => {
        const methodPathFormat = `${resource.method}:${resource.path}`
        return selectedPermissions.includes(resource.id) || selectedPermissions.includes(methodPathFormat)
    }

    const handleRemovePermission = (permissionId: string) => {
        setSelectedPermissions(prev => prev.filter(id => id !== permissionId))
    }

    return (
        <div className="space-y-2">
            <div className="flex items-center justify-between">
                <div className="text-sm font-medium">
                    API资源 {selectedPermissions.length > 0 && `(${selectedPermissions.length})`}
                </div>
                <ApiResourceSelector
                    apiResources={apiResources}
                    isLoading={isLoading}
                    selectedApiResources={selectedPermissions}
                    onResourceSelect={handleResourceSelect}
                    isResourceSelected={isResourceSelected}
                />
            </div>

            {/* Selected permissions display */}
            {selectedPermissions.length > 0 && (
                <div className="flex flex-wrap gap-2">
                    {selectedPermissions.map((resourceId) => {
                        // Find the corresponding API resource for this ID
                        // Support both direct ID matching and method:path format matching
                        const matchingResource = apiResources.find(resource => {
                            // Direct ID match
                            if (resource.id === resourceId) return true

                            // Method:path format match (e.g., "POST:/api/v1/system/menu")
                            const methodPathFormat = `${resource.method}:${resource.path}`
                            if (methodPathFormat === resourceId) return true

                            return false
                        })

                        return matchingResource ? (
                            <Badge
                                key={resourceId}
                                variant="secondary"
                                className="flex items-center space-x-1 pr-1"
                            >
                                <Badge
                                    className={`text-xs mr-1 ${METHOD_COLORS[matchingResource.method as keyof typeof METHOD_COLORS] || 'bg-gray-100 text-gray-800'}`}
                                >
                                    {matchingResource.method}
                                </Badge>
                                <span>{matchingResource.path}</span>
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    className="h-auto w-auto p-0.5 hover:bg-transparent"
                                    onClick={() => handleRemovePermission(resourceId)}
                                >
                                    <X className="h-3 w-3"/>
                                </Button>
                            </Badge>
                        ) : (
                            <Badge
                                key={resourceId}
                                variant="outline"
                                className="flex items-center space-x-1 pr-1 text-muted-foreground"
                            >
                                <span>{resourceId}</span>
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    className="h-auto w-auto p-0.5 hover:bg-transparent"
                                    onClick={() => handleRemovePermission(resourceId)}
                                >
                                    <X className="h-3 w-3"/>
                                </Button>
                            </Badge>
                        )
                    })}
                </div>
            )}
        </div>
    )
}