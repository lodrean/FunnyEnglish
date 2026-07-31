import type { Meta, StoryObj } from '@storybook/react';
import { Button } from '@mui/material';
import { Add, Save, Delete } from '@mui/icons-material';

const meta: Meta<typeof Button> = {
  title: 'Components/Button',
  component: Button,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
  argTypes: {
    variant: {
      control: 'select',
      options: ['text', 'outlined', 'contained'],
    },
    color: {
      control: 'select',
      options: ['primary', 'secondary', 'error', 'warning', 'info', 'success'],
    },
    size: {
      control: 'select',
      options: ['small', 'medium', 'large'],
    },
    disabled: {
      control: 'boolean',
    },
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Primary: Story = {
  args: {
    variant: 'contained',
    children: 'Button',
  },
};

export const Secondary: Story = {
  args: {
    variant: 'outlined',
    children: 'Button',
  },
};

export const WithIcon: Story = {
  args: {
    variant: 'contained',
    startIcon: <Add />,
    children: 'Add New',
  },
};

export const SaveButton: Story = {
  args: {
    variant: 'contained',
    color: 'primary',
    startIcon: <Save />,
    children: 'Save',
  },
};

export const DeleteButton: Story = {
  args: {
    variant: 'contained',
    color: 'error',
    startIcon: <Delete />,
    children: 'Delete',
  },
};

export const Disabled: Story = {
  args: {
    variant: 'contained',
    disabled: true,
    children: 'Disabled',
  },
};

export const Loading: Story = {
  args: {
    variant: 'contained',
    disabled: true,
    children: 'Loading...',
  },
};
