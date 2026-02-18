import {z} from 'zod'
import {useForm} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'
import {useMutation, useQueryClient} from '@tanstack/react-query'
import {
    Dialog,
    DialogClose,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle
} from '@/components/ui/dialog'
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle
} from '@/components/ui/alert-dialog'
import {Button} from '@/components/ui/button'
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from '@/components/ui/form'
import {Input} from '@/components/ui/input'
import {Textarea} from '@/components/ui/textarea'
import {Checkbox} from '@/components/ui/checkbox'
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from '@/components/ui/select'
import {createDictItem, deleteDictItem, updateDictItem} from '@/services/dictApi'
import {useDicts} from './dicts-provider'
import {toast} from 'sonner'

type DictItemStatus = 'active' | 'inactive'

const formSchema = z.object({
    label: z.string().min(1, '请输入显示标签'),
    value: z.string().min(1, '请输入实际值'),
    sort: z.number().min(0),
    is_default: z.boolean(),
    status: z.enum(['active', 'inactive']),
    color: z.string(),
    remark: z.string()
})

type DictItemForm = z.infer<typeof formSchema>

interface ApiError {
    response?: {
        data?: {
            msg?: string
        }
    }
}

interface DictItemFormFieldsProps {
    form: ReturnType<typeof useForm<DictItemForm>>
}

function DictItemFormFields({form}: DictItemFormFieldsProps) {
    return (
        <div className="space-y-4">
            <FormField
                control={form.control}
                name="label"
                render={({field}) => (
                    <FormItem>
                        <FormLabel>标签 *</FormLabel>
                        <FormControl>
                            <Input placeholder="请输入显示标签" {...field} />
                        </FormControl>
                        <FormMessage/>
                    </FormItem>
                )}
            />
            <FormField
                control={form.control}
                name="value"
                render={({field}) => (
                    <FormItem>
                        <FormLabel>值 *</FormLabel>
                        <FormControl>
                            <Input placeholder="请输入实际值" {...field} />
                        </FormControl>
                        <FormMessage/>
                    </FormItem>
                )}
            />
            <div className="grid grid-cols-2 gap-4">
                <FormField
                    control={form.control}
                    name="sort"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>排序</FormLabel>
                            <FormControl>
                                <Input
                                    type="number"
                                    placeholder="0"
                                    {...field}
                                    onChange={(e) => field.onChange(parseInt(e.target.value) || 0)}
                                />
                            </FormControl>
                            <FormMessage/>
                        </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name="color"
                    render={({field}) => (
                        <FormItem>
                            <FormLabel>颜色</FormLabel>
                            <FormControl>
                                <Input placeholder="#FF0000" {...field} />
                            </FormControl>
                            <FormMessage/>
                        </FormItem>
                    )}
                />
            </div>
            <FormField
                control={form.control}
                name="is_default"
                render={({field}) => (
                    <FormItem className="flex flex-row items-start space-x-3 space-y-0">
                        <FormControl>
                            <Checkbox
                                checked={field.value}
                                onCheckedChange={field.onChange}
                            />
                        </FormControl>
                        <div className="space-y-1 leading-none">
                            <FormLabel>设为默认项</FormLabel>
                        </div>
                        <FormMessage/>
                    </FormItem>
                )}
            />
            <FormField
                control={form.control}
                name="status"
                render={({field}) => (
                    <FormItem>
                        <FormLabel>状态</FormLabel>
                        <Select onValueChange={field.onChange} defaultValue={field.value}>
                            <FormControl>
                                <SelectTrigger>
                                    <SelectValue placeholder="选择状态"/>
                                </SelectTrigger>
                            </FormControl>
                            <SelectContent>
                                <SelectItem value="active">启用</SelectItem>
                                <SelectItem value="inactive">禁用</SelectItem>
                            </SelectContent>
                        </Select>
                        <FormMessage/>
                    </FormItem>
                )}
            />
            <FormField
                control={form.control}
                name="remark"
                render={({field}) => (
                    <FormItem>
                        <FormLabel>备注</FormLabel>
                        <FormControl>
                            <Textarea placeholder="请输入备注" {...field} />
                        </FormControl>
                        <FormMessage/>
                    </FormItem>
                )}
            />
        </div>
    )
}

export function ItemsDialogs() {
    const {
        selectedType,
        showItemCreateDialog,
        setShowItemCreateDialog,
        showItemEditDialog,
        setShowItemEditDialog,
        showItemDeleteDialog,
        setShowItemDeleteDialog,
        currentItemRow,
        setCurrentItemRow,
    } = useDicts()

    const queryClient = useQueryClient()

    const createForm = useForm<DictItemForm>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            label: '',
            value: '',
            sort: 0,
            is_default: false,
            status: 'active',
            color: '',
            remark: ''
        }
    })

    const editForm = useForm<DictItemForm>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            label: '',
            value: '',
            sort: 0,
            is_default: false,
            status: 'active',
            color: '',
            remark: ''
        }
    })

    const handleApiError = (error: ApiError, defaultMessage: string) => {
        toast.error(error?.response?.data?.msg || defaultMessage)
    }

    const refreshData = () => {
        queryClient.invalidateQueries({queryKey: ['dictItems']})
    }

    const createMutation = useMutation({
        mutationFn: createDictItem,
        onSuccess: () => {
            setShowItemCreateDialog(false)
            createForm.reset()
            refreshData()
            toast.success('创建字典项成功')
        },
        onError: (error: ApiError) => handleApiError(error, '创建失败'),
    })

    const updateMutation = useMutation({
        mutationFn: ({id, data}: { id: string; data: DictItemForm }) => updateDictItem(id, data),
        onSuccess: () => {
            setShowItemEditDialog(false)
            setCurrentItemRow(null)
            refreshData()
            toast.success('更新字典项成功')
        },
        onError: (error: ApiError) => handleApiError(error, '更新失败'),
    })

    const deleteMutation = useMutation({
        mutationFn: deleteDictItem,
        onSuccess: () => {
            setShowItemDeleteDialog(false)
            setCurrentItemRow(null)
            refreshData()
            toast.success('删除字典项成功')
        },
        onError: (error: ApiError) => handleApiError(error, '删除失败'),
    })

    const onCreateSubmit = async (values: DictItemForm) => {
        if (!selectedType) {
            toast.error('请先选择字典类型')
            return
        }
        try {
            await createMutation.mutateAsync({type_id: selectedType.id, ...values})
        } catch (error) {
            console.error('Error creating dict item:', error)
        }
    }

    const onEditSubmit = async (values: DictItemForm) => {
        if (!currentItemRow) return
        try {
            await updateMutation.mutateAsync({id: currentItemRow.id, data: values})
        } catch (error) {
            console.error('Error updating dict item:', error)
        }
    }

    const handleDelete = () => {
        if (!currentItemRow) return
        deleteMutation.mutate(currentItemRow.id)
    }

    const handleEditDialogChange = (open: boolean) => {
        setShowItemEditDialog(open)
        if (open && currentItemRow) {
            editForm.reset({
                label: currentItemRow.label,
                value: currentItemRow.value,
                sort: currentItemRow.sort,
                is_default: currentItemRow.is_default,
                status: currentItemRow.status as DictItemStatus,
                color: currentItemRow.color || '',
                remark: currentItemRow.remark || '',
            })
        }
    }

    const handleCreateDialogChange = (open: boolean) => {
        setShowItemCreateDialog(open)
        if (!open) {
            createForm.reset()
        }
    }

    return (
        <>
            {/* 创建对话框 */}
            <Dialog open={showItemCreateDialog} onOpenChange={handleCreateDialogChange}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>创建字典项</DialogTitle>
                        <DialogDescription>为字典类型 “{selectedType?.name}” 创建新的字典项</DialogDescription>
                    </DialogHeader>
                    <Form {...createForm}>
                        <form
                            id="create-item-form"
                            onSubmit={createForm.handleSubmit(onCreateSubmit)}
                            className="space-y-4"
                        >
                            <DictItemFormFields form={createForm}/>
                        </form>
                    </Form>
                    <DialogFooter>
                        <DialogClose asChild>
                            <Button variant="outline" disabled={createMutation.isPending}>
                                取消
                            </Button>
                        </DialogClose>
                        <Button
                            type="submit"
                            form="create-item-form"
                            disabled={createMutation.isPending}
                        >
                            {createMutation.isPending ? '创建中...' : '创建'}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* 编辑对话框 */}
            <Dialog open={showItemEditDialog} onOpenChange={handleEditDialogChange}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>编辑字典项</DialogTitle>
                        <DialogDescription>修改字典项信息</DialogDescription>
                    </DialogHeader>
                    <Form {...editForm}>
                        <form
                            id="edit-item-form"
                            onSubmit={editForm.handleSubmit(onEditSubmit)}
                            className="space-y-4"
                        >
                            <DictItemFormFields form={editForm}/>
                        </form>
                    </Form>
                    <DialogFooter>
                        <DialogClose asChild>
                            <Button variant="outline" disabled={updateMutation.isPending}>
                                取消
                            </Button>
                        </DialogClose>
                        <Button
                            type="submit"
                            form="edit-item-form"
                            disabled={updateMutation.isPending}
                        >
                            {updateMutation.isPending ? '保存中...' : '保存'}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* 删除确认对话框 */}
            <AlertDialog open={showItemDeleteDialog} onOpenChange={setShowItemDeleteDialog}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>确认删除</AlertDialogTitle>
                        <AlertDialogDescription>
                            您确定要删除字典项 “{currentItemRow?.label}” 吗？
                            <br/>
                            此操作不可恢复，请谨慎操作。
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>取消</AlertDialogCancel>
                        <AlertDialogAction
                            onClick={handleDelete}
                            disabled={deleteMutation.isPending}
                        >
                            {deleteMutation.isPending ? '删除中...' : '确认删除'}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </>
    )
}
