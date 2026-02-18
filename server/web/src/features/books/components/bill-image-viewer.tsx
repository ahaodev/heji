import { useState, useEffect } from 'react'
import { ImageIcon } from 'lucide-react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Skeleton } from '@/components/ui/skeleton'
import { getBillImages } from '@/services/imageApi'
import { ACCESS_TOKEN } from '@/types/constants'
import type { BillImage } from '@/types/bill'

interface BillImageViewerProps {
  billId: string
}

export function BillImageViewer({ billId }: BillImageViewerProps) {
  const [images, setImages] = useState<BillImage[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [selectedImage, setSelectedImage] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return
    setLoading(true)
    getBillImages(billId)
      .then(setImages)
      .catch(() => setImages([]))
      .finally(() => setLoading(false))
  }, [billId, open])

  const token = localStorage.getItem(ACCESS_TOKEN)
  const getImageSrc = (imageId: string) =>
    `/api/v1/image/${imageId}?token=${token}`

  return (
    <>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger asChild>
          <button className='inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground transition-colors'>
            <ImageIcon className='size-4' />
          </button>
        </DialogTrigger>
        <DialogContent className='max-w-2xl'>
          <DialogHeader>
            <DialogTitle>账单照片</DialogTitle>
          </DialogHeader>
          {loading ? (
            <div className='grid grid-cols-3 gap-3'>
              {Array.from({ length: 3 }).map((_, i) => (
                <Skeleton key={i} className='aspect-square rounded-lg' />
              ))}
            </div>
          ) : images.length === 0 ? (
            <div className='flex h-32 items-center justify-center text-muted-foreground'>
              暂无照片
            </div>
          ) : (
            <div className='grid grid-cols-3 gap-3'>
              {images.map((img) => (
                <button
                  key={img._id}
                  className='group relative aspect-square overflow-hidden rounded-lg border bg-muted cursor-pointer'
                  onClick={() => setSelectedImage(img._id)}
                >
                  <img
                    src={getImageSrc(img._id)}
                    alt='账单照片'
                    className='size-full object-cover transition-transform group-hover:scale-105'
                    loading='lazy'
                  />
                </button>
              ))}
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* 大图预览 */}
      <Dialog
        open={!!selectedImage}
        onOpenChange={() => setSelectedImage(null)}
      >
        <DialogContent className='max-w-4xl p-2'>
          <DialogHeader className='sr-only'>
            <DialogTitle>照片预览</DialogTitle>
          </DialogHeader>
          {selectedImage && (
            <img
              src={getImageSrc(selectedImage)}
              alt='账单照片'
              className='w-full rounded-lg object-contain max-h-[80vh]'
            />
          )}
        </DialogContent>
      </Dialog>
    </>
  )
}
