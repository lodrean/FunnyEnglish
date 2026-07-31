import React from 'react';
import {
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormHelperText,
  OutlinedTextFieldProps,
  SelectProps,
} from '@mui/material';
import { Controller, Control, FieldValues, Path, RegisterOptions } from 'react-hook-form';

// Design System Colors
const colors = {
  primary: '#4A90D9',
  error: '#E53935',
  textPrimary: '#212121',
  textSecondary: '#757575',
};

// Form field types supported
export type FormFieldType = 
  | 'text' 
  | 'email' 
  | 'password' 
  | 'number' 
  | 'select' 
  | 'multiline' 
  | 'date';

// Select option type
export interface SelectOption {
  value: string | number;
  label: string;
}

// Form field props
export interface FormFieldProps<T extends FieldValues> {
  /** Field name - must match form schema */
  name: Path<T>;
  /** react-hook-form control instance */
  control: Control<T>;
  /** Field type */
  type?: FormFieldType;
  /** Field label */
  label?: string;
  /** Placeholder text */
  placeholder?: string;
  /** Helper text displayed below field */
  helperText?: string;
  /** Whether field is required */
  required?: boolean;
  /** Options for select type */
  options?: SelectOption[];
  /** Minimum value (for number) or length (for text) */
  min?: number;
  /** Maximum value (for number) or length (for text) */
  max?: number;
  /** Pattern for validation */
  pattern?: RegExp;
  /** Custom validation rules */
  rules?: Omit<RegisterOptions<T, Path<T>>, 'required' | 'min' | 'max' | 'pattern'>;
  /** Number of rows for multiline */
  rows?: number;
  /** Whether field is disabled */
  disabled?: boolean;
  /** Whether field is read-only */
  readOnly?: boolean;
  /** Full width styling */
  fullWidth?: boolean;
  /** Custom class name */
  className?: string;
  /** Auto-focus on mount */
  autoFocus?: boolean;
  /** Input props for TextField */
  InputProps?: OutlinedTextFieldProps['InputProps'];
  /** Change callback */
  onChange?: (value: unknown) => void;
}

/**
 * Universal Form Field Component
 * 
 * A wrapper around MUI form inputs with react-hook-form integration.
 * Supports multiple input types with consistent validation and error handling.
 * 
 * @example
 * ```tsx
 * <FormField
 *   name="email"
 *   control={control}
 *   type="email"
 *   label="Email Address"
 *   required
 *   helperText="Enter your email"
 * />
 * 
 * <FormField
 *   name="role"
 *   control={control}
 *   type="select"
 *   label="Role"
 *   options={[
 *     { value: 'admin', label: 'Administrator' },
 *     { value: 'user', label: 'User' }
 *   ]}
 * />
 * ```
 */
export function FormField<T extends FieldValues>({
  name,
  control,
  type = 'text',
  label,
  placeholder,
  helperText,
  required = false,
  options = [],
  min,
  max,
  pattern,
  rules,
  rows = 4,
  disabled = false,
  readOnly = false,
  fullWidth = true,
  className,
  autoFocus = false,
  InputProps,
  onChange: onChangeCallback,
}: FormFieldProps<T>): React.ReactElement {
  
  // Build validation rules
  const validationRules: RegisterOptions<T, Path<T>> = {
    required: required ? `${label || name} is required` : false,
    ...rules,
  };

  // Add min/max validation based on type
  if (type === 'number') {
    if (min !== undefined) {
      validationRules.min = {
        value: min,
        message: `Minimum value is ${min}`,
      };
    }
    if (max !== undefined) {
      validationRules.max = {
        value: max,
        message: `Maximum value is ${max}`,
      };
    }
  } else {
    if (min !== undefined) {
      validationRules.minLength = {
        value: min,
        message: `Minimum length is ${min} characters`,
      };
    }
    if (max !== undefined) {
      validationRules.maxLength = {
        value: max,
        message: `Maximum length is ${max} characters`,
      };
    }
  }

  // Add pattern validation
  if (pattern) {
    validationRules.pattern = {
      value: pattern,
      message: `Invalid format for ${label || name}`,
    };
  }

  // Render select field
  if (type === 'select') {
    return (
      <Controller
        name={name}
        control={control}
        rules={validationRules}
        render={({ field, fieldState: { error } }) => (
          <FormControl
            fullWidth={fullWidth}
            error={!!error}
            disabled={disabled}
            className={className}
            size="small"
          >
            {label && (
              <InputLabel id={`${name}-label`} required={required}>
                {label}
              </InputLabel>
            )}
            <Select
              {...field}
              labelId={label ? `${name}-label` : undefined}
              label={label}
              value={field.value ?? ''}
              onChange={(e) => {
                field.onChange(e);
                onChangeCallback?.(e.target.value);
              }}
              readOnly={readOnly}
              sx={{
                '& .MuiOutlinedInput-notchedOutline': {
                  borderColor: error ? colors.error : undefined,
                },
                '&:hover .MuiOutlinedInput-notchedOutline': {
                  borderColor: error ? colors.error : colors.primary,
                },
                '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                  borderColor: colors.primary,
                },
              }}
            >
              {options.map((option) => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
            {(error || helperText) && (
              <FormHelperText error={!!error}>
                {error?.message || helperText}
              </FormHelperText>
            )}
          </FormControl>
        )}
      />
    );
  }

  // Render text-based fields
  return (
    <Controller
      name={name}
      control={control}
      rules={validationRules}
      render={({ field, fieldState: { error } }) => (
        <TextField
          {...field}
          type={type === 'multiline' ? 'text' : type}
          label={label}
          placeholder={placeholder}
          required={required}
          disabled={disabled}
          fullWidth={fullWidth}
          error={!!error}
          helperText={error?.message || helperText}
          multiline={type === 'multiline'}
          rows={type === 'multiline' ? rows : undefined}
          autoFocus={autoFocus}
          InputProps={{
            readOnly,
            ...InputProps,
          }}
          className={className}
          size="small"
          value={field.value ?? ''}
          onChange={(e) => {
            let value: string | number = e.target.value;
            
            // Handle number type conversion
            if (type === 'number' && value !== '') {
              value = Number(value);
            }
            
            field.onChange(value);
            onChangeCallback?.(value);
          }}
          sx={{
            '& .MuiOutlinedInput-root': {
              '& fieldset': {
                borderColor: error ? colors.error : undefined,
              },
              '&:hover fieldset': {
                borderColor: error ? colors.error : colors.primary,
              },
              '&.Mui-focused fieldset': {
                borderColor: colors.primary,
              },
            },
            '& .MuiInputLabel-root': {
              color: colors.textSecondary,
              '&.Mui-focused': {
                color: colors.primary,
              },
            },
            '& .MuiFormHelperText-root': {
              color: error ? colors.error : colors.textSecondary,
            },
          }}
        />
      )}
    />
  );
}

export default FormField;
