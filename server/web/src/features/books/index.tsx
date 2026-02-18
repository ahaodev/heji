import { ConfigDrawer } from '@/components/config-drawer'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { ThemeSwitch } from '@/components/theme-switch'
import { Skeleton } from '@/components/ui/skeleton'
import { BooksTable } from './components/books-table'
import { useBooks } from './hooks/use-books'

export function Books() {
  const { data: books, isLoading, error } = useBooks()

  return (
    <>
      <Header fixed>
        <Search />
        <div className='ms-auto flex items-center space-x-4'>
          <ThemeSwitch />
          <ConfigDrawer />
          <ProfileDropdown />
        </div>
      </Header>

      <Main>
        <div className='mb-2 flex flex-wrap items-center justify-between space-y-2'>
          <div>
            <h2 className='text-2xl font-bold tracking-tight'>账本管理</h2>
            <p className='text-muted-foreground'>
              浏览您的所有账本，点击查看账单详情。
            </p>
          </div>
        </div>
        <div className='-mx-4 flex-1 overflow-auto px-4 py-1 lg:flex-row lg:space-y-0 lg:space-x-12'>
          {isLoading ? (
            <div className='space-y-4'>
              <div className='flex items-center justify-between'>
                <Skeleton className='h-8 w-48' />
                <Skeleton className='h-10 w-32' />
              </div>
              <div className='space-y-2'>
                {Array.from({ length: 5 }).map((_, i) => (
                  <Skeleton key={i} className='h-16 w-full' />
                ))}
              </div>
            </div>
          ) : error ? (
            <div className='flex h-32 items-center justify-center text-muted-foreground'>
              加载账本数据失败，请重试
            </div>
          ) : (
            <BooksTable data={books || []} />
          )}
        </div>
      </Main>
    </>
  )
}
