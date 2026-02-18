import {type UseFormReturn} from 'react-hook-form'
import {FormControl, FormField, FormItem, FormLabel, FormMessage} from '@/components/ui/form'
import {Input} from '@/components/ui/input'
import {PasswordInput} from '@/components/password-input'
import {SelectDropdown} from '@/components/select-dropdown'
import {MultiSelectDropdown} from '@/components/multi-select-dropdown'
import {statusOptions, type UserFormData} from '../lib/user-form-schema'
import type {RoleInfo} from '@/types/role'

interface UserFormFieldsProps {
    form: UseFormReturn<UserFormData>
    isEdit: boolean
    isPasswordTouched: boolean
    allRoles?: RoleInfo[]
}

export function UserFormFields({
                                   form,
                                   isPasswordTouched,
                                   allRoles = []
                               }: UserFormFieldsProps) {
    return (
        <>
            <FormField
                control={form.control}
                name='username'
                render={({field}) => (
                    <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                        <FormLabel className='col-span-2 text-end'>
                            用户名
                        </FormLabel>
                        <FormControl>
                            <Input
                                placeholder='用户名'
                                className='col-span-4'
                                {...field}
                            />
                        </FormControl>
                        <FormMessage className='col-span-4 col-start-3'/>
                    </FormItem>
                )}
            />

            <FormField
                control={form.control}
                name='email'
                render={({field}) => (
                    <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                        <FormLabel className='col-span-2 text-end'>邮箱</FormLabel>
                        <FormControl>
                            <Input
                                placeholder='例如：john.doe@gmail.com'
                                className='col-span-4'
                                {...field}
                            />
                        </FormControl>
                        <FormMessage className='col-span-4 col-start-3'/>
                    </FormItem>
                )}
            />

            <FormField
                control={form.control}
                name='phone'
                render={({field}) => (
                    <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                        <FormLabel className='col-span-2 text-end'>
                            手机号码
                        </FormLabel>
                        <FormControl>
                            <Input
                                placeholder='+86 13812345678'
                                className='col-span-4'
                                {...field}
                            />
                        </FormControl>
                        <FormMessage className='col-span-4 col-start-3'/>
                    </FormItem>
                )}
            />


            <FormField
                control={form.control}
                name='status'
                render={({field}) => (
                    <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                        <FormLabel className='col-span-2 text-end'>状态</FormLabel>
                        <SelectDropdown
                            defaultValue={field.value}
                            onValueChange={field.onChange}
                            placeholder='选择状态'
                            className='col-span-4'
                            items={[...statusOptions]}
                        />
                        <FormMessage className='col-span-4 col-start-3'/>
                    </FormItem>
                )}
            />

            <FormField
                control={form.control}
                name='password'
                render={({field}) => (
                    <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                        <FormLabel className='col-span-2 text-end'>
                            密码
                        </FormLabel>
                        <FormControl>
                            <PasswordInput
                                placeholder='例如：S3cur3P@ssw0rd'
                                className='col-span-4'
                                {...field}
                            />
                        </FormControl>
                        <FormMessage className='col-span-4 col-start-3'/>
                    </FormItem>
                )}
            />

            <FormField
                control={form.control}
                name='confirmPassword'
                render={({field}) => (
                    <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                        <FormLabel className='col-span-2 text-end'>
                            确认密码
                        </FormLabel>
                        <FormControl>
                            <PasswordInput
                                disabled={!isPasswordTouched}
                                placeholder='例如：S3cur3P@ssw0rd'
                                className='col-span-4'
                                {...field}
                            />
                        </FormControl>
                        <FormMessage className='col-span-4 col-start-3'/>
                    </FormItem>
                )}
            />

            <FormField
                control={form.control}
                name='roles'
                render={({field}) => {
                    const selectedRoles = field.value || []

                    const handleRoleChange = (roleIds: string[]) => {
                        const roleNames = roleIds.map(id => {
                            const role = allRoles.find(r => r.id === id)
                            return role?.name || id
                        })
                        field.onChange(roleNames)
                    }

                    const selectedRoleIds = selectedRoles.map((roleName: string) => {
                        const role = allRoles.find(r => r.name === roleName)
                        return role?.id || roleName
                    })

                    const roleItems = allRoles.map(role => ({
                        label: role.name,
                        value: role.id
                    }))

                    return (
                        <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                            <FormLabel className='col-span-2 text-end'>
                                角色
                            </FormLabel>
                            <FormControl>
                                <MultiSelectDropdown
                                    value={selectedRoleIds}
                                    onValueChange={handleRoleChange}
                                    items={roleItems}
                                    placeholder="选择角色"
                                    className='col-span-4'
                                />
                            </FormControl>
                            <FormMessage className='col-span-4 col-start-3'/>
                        </FormItem>
                    )
                }}
            />
        </>
    )
}