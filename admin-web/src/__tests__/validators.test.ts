import { describe, it, expect } from 'vitest';
import {
  emailSchema,
  passwordSchema,
  nameSchema,
  urlSchema,
  phoneSchema,
  validateFileSize,
  validateFileType,
  requiredString,
  optionalString,
  positiveNumber,
  nonNegativeNumber,
  testSchema,
  questionSchema,
  userSchema,
  categorySchema,
  createZodResolver,
} from '../utils/validators';

describe('validators', () => {
  describe('emailSchema', () => {
    it('should validate correct emails', () => {
      expect(emailSchema.safeParse('test@example.com').success).toBe(true);
      expect(emailSchema.safeParse('user.name@domain.co.uk').success).toBe(true);
    });

    it('should reject invalid emails', () => {
      expect(emailSchema.safeParse('').success).toBe(false);
      expect(emailSchema.safeParse('invalid').success).toBe(false);
      expect(emailSchema.safeParse('@example.com').success).toBe(false);
      expect(emailSchema.safeParse('test@').success).toBe(false);
    });
  });

  describe('passwordSchema', () => {
    it('should validate strong passwords', () => {
      expect(passwordSchema.safeParse('Password123').success).toBe(true);
      expect(passwordSchema.safeParse('MyP@ssw0rd').success).toBe(true);
    });

    it('should reject weak passwords', () => {
      expect(passwordSchema.safeParse('short').success).toBe(false);
      expect(passwordSchema.safeParse('password').success).toBe(false);
      expect(passwordSchema.safeParse('PASSWORD').success).toBe(false);
      expect(passwordSchema.safeParse('12345678').success).toBe(false);
    });
  });

  describe('nameSchema', () => {
    it('should validate valid names', () => {
      expect(nameSchema.safeParse('John').success).toBe(true);
      expect(nameSchema.safeParse('John Doe').success).toBe(true);
      expect(nameSchema.safeParse("O'Connor").success).toBe(true);
    });

    it('should reject invalid names', () => {
      expect(nameSchema.safeParse('').success).toBe(false);
      expect(nameSchema.safeParse('A').success).toBe(false);
      expect(nameSchema.safeParse('Name@123').success).toBe(false);
    });
  });

  describe('urlSchema', () => {
    it('should validate valid URLs', () => {
      expect(urlSchema.safeParse('https://example.com').success).toBe(true);
      expect(urlSchema.safeParse('http://localhost:3000').success).toBe(true);
    });

    it('should allow empty strings', () => {
      expect(urlSchema.safeParse('').success).toBe(true);
      expect(urlSchema.safeParse(undefined).success).toBe(true);
    });

    it('should reject invalid URLs', () => {
      expect(urlSchema.safeParse('not-a-url').success).toBe(false);
    });
  });

  describe('phoneSchema', () => {
    it('should validate valid phone numbers', () => {
      expect(phoneSchema.safeParse('+1234567890').success).toBe(true);
      expect(phoneSchema.safeParse('+1 (234) 567-890').success).toBe(true);
    });

    it('should allow empty strings', () => {
      expect(phoneSchema.safeParse('').success).toBe(true);
    });

    it('should reject invalid phone numbers', () => {
      expect(phoneSchema.safeParse('abc').success).toBe(false);
    });
  });

  describe('validateFileSize', () => {
    it('should validate file size within limit', () => {
      const file = { size: 1024 * 1024 } as File; // 1 MB
      expect(validateFileSize(file, 2)).toBe(true);
    });

    it('should reject file size exceeding limit', () => {
      const file = { size: 1024 * 1024 } as File; // 1 MB
      expect(validateFileSize(file, 0.5)).toBe(false);
    });
  });

  describe('validateFileType', () => {
    it('should validate file type', () => {
      const file = new File([''], 'test.png', { type: 'image/png' });
      expect(validateFileType(file, ['image/png', 'image/jpeg'])).toBe(true);
      expect(validateFileType(file, ['image/jpeg'])).toBe(false);
    });
  });

  describe('requiredString', () => {
    it('should validate non-empty strings', () => {
      expect(requiredString().safeParse('hello').success).toBe(true);
    });

    it('should reject empty strings', () => {
      expect(requiredString().safeParse('').success).toBe(false);
    });

    it('should use custom message', () => {
      const result = requiredString('Custom error').safeParse('');
      if (!result.success) {
        expect(result.error.errors[0].message).toBe('Custom error');
      }
    });
  });

  describe('optionalString', () => {
    it('should allow strings or empty', () => {
      expect(optionalString.safeParse('hello').success).toBe(true);
      expect(optionalString.safeParse('').success).toBe(true);
      expect(optionalString.safeParse(undefined).success).toBe(true);
    });
  });

  describe('positiveNumber', () => {
    it('should validate positive numbers', () => {
      expect(positiveNumber.safeParse(5).success).toBe(true);
      expect(positiveNumber.safeParse('10').success).toBe(true);
    });

    it('should reject non-positive numbers', () => {
      expect(positiveNumber.safeParse(0).success).toBe(false);
      expect(positiveNumber.safeParse(-5).success).toBe(false);
    });
  });

  describe('nonNegativeNumber', () => {
    it('should validate non-negative numbers', () => {
      expect(nonNegativeNumber.safeParse(0).success).toBe(true);
      expect(nonNegativeNumber.safeParse(5).success).toBe(true);
    });

    it('should reject negative numbers', () => {
      expect(nonNegativeNumber.safeParse(-1).success).toBe(false);
    });
  });

  describe('testSchema', () => {
    it('should validate valid test data', () => {
      const validTest = {
        title: 'Test Title',
        categoryId: 'cat123',
        difficulty: 'easy',
        passingScore: 70,
      };
      expect(testSchema.safeParse(validTest).success).toBe(true);
    });

    it('should reject invalid test data', () => {
      expect(testSchema.safeParse({ title: '' }).success).toBe(false);
      expect(testSchema.safeParse({ title: 'Test' }).success).toBe(false);
    });
  });

  describe('questionSchema', () => {
    it('should validate valid question', () => {
      const validQuestion = {
        text: 'Question text?',
        type: 'TEXT_SELECT',
        points: 10,
        options: [
          { id: '1', text: 'Option 1', isCorrect: true },
          { id: '2', text: 'Option 2', isCorrect: false },
        ],
      };
      expect(questionSchema.safeParse(validQuestion).success).toBe(true);
    });

    it('should reject questions with less than 2 options', () => {
      const invalidQuestion = {
        text: 'Question?',
        type: 'TEXT_SELECT',
        points: 10,
        options: [{ id: '1', text: 'Only option', isCorrect: true }],
      };
      expect(questionSchema.safeParse(invalidQuestion).success).toBe(false);
    });
  });

  describe('userSchema', () => {
    it('should validate valid user', () => {
      const validUser = {
        name: 'John Doe',
        email: 'john@example.com',
        role: 'admin',
        status: 'active',
      };
      expect(userSchema.safeParse(validUser).success).toBe(true);
    });

    it('should reject invalid user data', () => {
      expect(userSchema.safeParse({ name: 'J', email: 'invalid' }).success).toBe(false);
    });
  });

  describe('categorySchema', () => {
    it('should validate valid category', () => {
      const validCategory = {
        name: 'Grammar',
      };
      expect(categorySchema.safeParse(validCategory).success).toBe(true);
    });

    it('should reject empty category name', () => {
      expect(categorySchema.safeParse({ name: '' }).success).toBe(false);
    });
  });

  describe('createZodResolver', () => {
    it('should return valid data on success', async () => {
      const resolver = createZodResolver(emailSchema);
      const result = await resolver('test@example.com');
      expect(result.values).toBe('test@example.com');
      expect(result.errors).toEqual({});
    });

    it('should return errors on failure', async () => {
      const resolver = createZodResolver(emailSchema);
      const result = await resolver('invalid');
      expect(result.values).toEqual({});
      expect(Object.keys(result.errors).length).toBeGreaterThan(0);
    });
  });
});
