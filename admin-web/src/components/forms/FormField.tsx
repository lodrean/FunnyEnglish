/**
 * FormField Component - Universal form input with validation
 * Design System 2.0 - Integrates with react-hook-form
 */

import React from 'react';
import {
  TextField,
  TextFieldProps,
  FormHelperText,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Switch,
  Checkbox,
  Radio,
  RadioGroup,
  FormLabel,
  Box,
  Chip,
  OutlinedInput,
  ListItemText,
  alpha,
  useTheme,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { Controller, useFormContext } from 'react-hook-form';

// =============================================================================
// TYPES
// =============================================================================

export type FormFieldType =
  | 'text'
  | 'email'
  | 'password'
  | 'number'
  | 'multiline'
  | 'select'
  | 'multiselect'
  | 'date'
  | 'switch'
  | 'checkbox'
  | 'radio'
  | 'file';

export interface SelectOption {
  value: string;
  label: string;
  disabled?: boolean;
}

export interface FormFieldProps extends Omit<TextFieldProps, 'name' | 'select'> {
  name: string;
  label: string;
  type?: FormFieldType;
  options?: SelectOption[];
  helperText?: string;
  validation?: Record<string, unknown>;
  placeholder?: string;
  size?: 'small' | 'medium';
  fullWidth?: boolean;
  disabled?: boolean;
  required?: boolean;
  multiline?: boolean;
  rows?: number;
  min?: number;
  max?: number;
  step?: number;
  accept?: string;
  radioOptions?: SelectOption[];
  maxSelect?: number;
}

// =============================================================================
// MAIN COMPONENT
// =============================================================================

export const FormField: React.FC<FormFieldProps> = ({
  name,
  label,
  type = 'text',
  options,
  helperText,
  validation,
  placeholder,
  size = 'medium',
  fullWidth = true,
  disabled = false,
  required = false,
  multiline = false,
  rows = 4,
  min,
  max,
  step,
  accept,
  radioOptions,
  maxSelect,
  ...textFieldProps
}) => {
  const theme = useTheme();
  const {
    control,
    formState: { errors },
    watch,
  } = useFormContext();

  const error = errors[name];
  const errorMessage = error?.message as string | undefined;

  // Switch type
  if (type === 'switch') {
    return (
      <Controller
        name={name}
        control={control}
        rules={validation}
        render={({ field }) => (
          <FormControl
            fullWidth={fullWidth}
            error={!!error}
            disabled={disabled}
          >
            <FormControlLabel
              control={
                <Switch
                  {...field}
                  checked={field.value}
                  color="primary"
                />
              }
              label={label}
            />
            {(errorMessage || helperText) && (
              <FormHelperText error={!!error}>
                {errorMessage || helperText}
              </FormHelperText>
            )}
          </FormControl>
        )}
      />
    );
  }

  // Checkbox type
  if (type === 'checkbox') {
    return (
      <Controller
        name={name}
        control={control}
        rules={validation}
        render={({ field }) => (
          <FormControl
            fullWidth={fullWidth}
            error={!!error}
            disabled={disabled}
          >
            <FormControlLabel
              control={
                <Checkbox
                  {...field}
                  checked={field.value}
                  color="primary"
                />
              }
              label={label}
            />
            {(errorMessage || helperText) && (
              <FormHelperText error={!!error}>
                {errorMessage || helperText}
              </FormHelperText>
            )}
          </FormControl>
        )}
      />
    );
  }

  // Radio type
  if (type === 'radio' && radioOptions) {
    return (
      <Controller
        name={name}
        control={control}
        rules={validation}
        render={({ field }) => (
          <FormControl
            fullWidth={fullWidth}
            error={!!error}
            disabled={disabled}
          >
            <FormLabel component="legend">{label}</FormLabel>
            <RadioGroup {...field} row>
              {radioOptions.map((opt) => (
                <FormControlLabel
                  key={opt.value}
                  value={opt.value}
                  control={<Radio />}
                  label={opt.label}
                  disabled={opt.disabled}
                />
              ))}
            </RadioGroup>
            {(errorMessage || helperText) && (
              <FormHelperText error={!!error}>
                {errorMessage || helperText}
              </FormHelperText>
            )}
          </FormControl>
        )}
      />
    );
  }

  // Select type
  if (type === 'select' && options) {
    return (
      <Controller
        name={name}
        control={control}
        rules={validation}
        render={({ field }) => (
          <FormControl
            fullWidth={fullWidth}
            error={!!error}
            disabled={disabled}
            required={required}
          >
            <InputLabel size={size === 'medium' ? 'normal' : 'small'}>{label}</InputLabel>
            <Select
              {...field}
              label={label}
              size={size}
              displayEmpty={!!placeholder}
            >
              {placeholder && (
                <MenuItem value="" disabled>
                  {placeholder}
                </MenuItem>
              )}
              {options.map((opt) => (
                <MenuItem
                  key={opt.value}
                  value={opt.value}
                  disabled={opt.disabled}
                >
                  {opt.label}
                </MenuItem>
              ))}
            </Select>
            {(errorMessage || helperText) && (
              <FormHelperText error={!!error}>
                {errorMessage || helperText}
              </FormHelperText>
            )}
          </FormControl>
        )}
      />
    );
  }

  // Multi-select type
  if (type === 'multiselect' && options) {
    const selectedValues = watch(name) || [];

    return (
      <Controller
        name={name}
        control={control}
        rules={validation}
        render={({ field }) => (
          <FormControl
            fullWidth={fullWidth}
            error={!!error}
            disabled={disabled}
            required={required}
          >
            <InputLabel size={size === 'medium' ? 'normal' : 'small'}>{label}</InputLabel>
            <Select
              {...field}
              multiple
              label={label}
              size={size}
              input={<OutlinedInput label={label} />}
              renderValue={(selected) => (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {(selected as string[]).map((value) => {
                    const option = options.find((o) => o.value === value);
                    return (
                      <Chip
                        key={value}
                        label={option?.label || value}
                        size="small"
                        sx={{
                          backgroundColor: alpha(theme.palette.primary.main, 0.1),
                          color: theme.palette.primary.main,
                        }}
                      />
                    );
                  })}
                </Box>
              )}
            >
              {options.map((opt) => (
                <MenuItem
                  key={opt.value}
                  value={opt.value}
                  disabled={opt.disabled}
                >
                  <Checkbox
                    checked={selectedValues.indexOf(opt.value) > -1}
                  />
                  <ListItemText primary={opt.label} />
                </MenuItem>
              ))}
            </Select>
            {(errorMessage || helperText) && (
              <FormHelperText error={!!error}>
                {errorMessage || helperText}
              </FormHelperText>
            )}
            {maxSelect && (
              <FormHelperText>
                {selectedValues.length} of {maxSelect} selected
              </FormHelperText>
            )}
          </FormControl>
        )}
      />
    );
  }

  // Date picker type
  if (type === 'date') {
    return (
      <Controller
        name={name}
        control={control}
        rules={validation}
        render={({ field }) => (
          <DatePicker
            {...field}
            label={label}
            disabled={disabled}
            slotProps={{
              textField: {
                fullWidth,
                size,
                error: !!error,
                helperText: errorMessage || helperText,
                required,
              },
            }}
          />
        )}
      />
    );
  }

  // Default text input
  return (
    <Controller
      name={name}
      control={control}
      rules={validation}
      render={({ field }) => (
        <TextField
          {...field}
          {...textFieldProps}
          label={label}
          type={type === 'multiline' ? 'text' : type}
          placeholder={placeholder}
          size={size}
          fullWidth={fullWidth}
          disabled={disabled}
          required={required}
          error={!!error}
          helperText={errorMessage || helperText}
          multiline={type === 'multiline' || multiline}
          rows={type === 'multiline' ? rows : undefined}
          inputProps={{
            min,
            max,
            step,
            accept,
          }}
        />
      )}
    />
  );
};

export default FormField;
