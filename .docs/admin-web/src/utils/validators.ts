import { z } from 'zod'

// Email validation
export const emailSchema = z
  .string()
  .min(1, 'Email is required')
  .email('Invalid email address')

// Password validation
export const passwordSchema = z
  .string()
  .min(8, 'Password must be at least 8 characters')
  .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
  .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
  .regex(/[0-9]/, 'Password must contain at least one number')

// Name validation
export const nameSchema = z
  .string()
  .min(2, 'Name must be at least 2 characters')
  .max(100, 'Name must be less than 100 characters')
  .regex(/^[a-zA-Z\s'-]+$/, 'Name contains invalid characters')

// URL validation
export const urlSchema = z
  .string()
  .url('Invalid URL format')
  .optional()
  .or(z.literal(''))

// Phone validation
export const phoneSchema = z
  .string()
  .regex(/^\+?[\d\s-()]+$/, 'Invalid phone number format')
  .optional()
  .or(z.literal(''))

// File validation helpers
export const validateFileSize = (file: File, maxSizeMB: number): boolean => {
  return file.size <= maxSizeMB * 1024 * 1024
}

export const validateFileType = (file: File, allowedTypes: string[]): boolean => {
  return allowedTypes.includes(file.type)
}

// Common validation schemas
export const requiredString = (message = 'This field is required') =>
  z.string().min(1, message)

export const optionalString = z.string().optional().or(z.literal(''))

export const positiveNumber = z
  .number()
  .positive('Must be a positive number')
  .or(z.string().regex(/^\d+$/).transform(Number))

export const nonNegativeNumber = z
  .number()
  .min(0, 'Must be 0 or greater')
  .or(z.string().regex(/^\d*\.?\d*$/).transform(Number))

// Test validation schemas
export const testSchema = z.object({
  title: requiredString('Test title is required'),
  categoryId: requiredString('Category is required'),
  description: z.string().optional(),
  difficulty: z.enum(['easy', 'medium', 'hard']),
  timeLimit: z.number().min(0).optional(),
  passingScore: z.number().min(0).max(100),
})

// Question validation schemas
export const questionSchema = z.object({
  text: requiredString('Question text is required'),
  type: z.enum(['TEXT_SELECT', 'IMAGE_SELECT', 'AUDIO_SELECT', 'DRAG_DROP', 'TEXT_INPUT']),
  points: z.number().min(1, 'Points must be at least 1'),
  options: z.array(z.object({
    id: z.string(),
    text: z.string(),
    isCorrect: z.boolean(),
  })).min(2, 'At least 2 options are required'),
})

// User validation schemas
export const userSchema = z.object({
  name: nameSchema,
  email: emailSchema,
  role: z.enum(['admin', 'editor', 'viewer']),
  status: z.enum(['active', 'inactive']),
})

// Category validation
export const categorySchema = z.object({
  name: requiredString('Category name is required'),
  parentId: z.string().optional(),
  description: z.string().optional(),
})

// Helper to create Zod resolver for react-hook-form
export const createZodResolver = <T extends z.ZodType<any, any>>(schema: T) => {
  return async (data: unknown) => {
    try {
      const validData = await schema.parseAsync(data)
      return { values: validData, errors: {} }
    } catch (error) {
      if (error instanceof z.ZodError) {
        const errors = error.errors.reduce((acc, curr) => {
          const path = curr.path.join('.')
          acc[path] = { message: curr.message, type: 'validation' }
          return acc
        }, {} as Record<string, { message: string; type: string }>)
        return { values: {}, errors }
      }
      throw error
    }
  }
}
