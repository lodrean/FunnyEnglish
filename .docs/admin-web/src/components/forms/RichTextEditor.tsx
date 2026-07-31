import React, { useCallback } from 'react';
import { useEditor, EditorContent, BubbleMenu, FloatingMenu } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Link from '@tiptap/extension-link';
import Placeholder from '@tiptap/extension-placeholder';
import {
  Box,
  Paper,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
  FormHelperText,
} from '@mui/material';
import {
  FormatBold,
  FormatItalic,
  FormatListBulleted,
  FormatListNumbered,
  Link as LinkIcon,
  Title,
} from '@mui/icons-material';
import { Controller, Control, FieldValues, Path } from 'react-hook-form';

// Design System Colors
const colors = {
  primary: '#4A90D9',
  error: '#E53935',
  textPrimary: '#212121',
  textSecondary: '#757575',
  background: '#F5F5F5',
  card: '#FFFFFF',
  border: '#E0E0E0',
};

// Rich text editor props
export interface RichTextEditorProps<T extends FieldValues> {
  /** Field name - must match form schema */
  name: Path<T>;
  /** react-hook-form control instance */
  control: Control<T>;
  /** Field label */
  label?: string;
  /** Placeholder text when editor is empty */
  placeholder?: string;
  /** Helper text displayed below editor */
  helperText?: string;
  /** Maximum height of editor content */
  maxHeight?: number | string;
  /** Minimum height of editor content */
  minHeight?: number | string;
  /** Whether field is required */
  required?: boolean;
  /** Whether field is disabled */
  disabled?: boolean;
  /** Custom class name */
  className?: string;
}

/**
 * Rich Text Editor Component
 * 
 * A WYSIWYG editor built with TipTap for rich content editing.
 * Features bold, italic, lists, and link formatting.
 * Outputs clean HTML format.
 * 
 * @example
 * ```tsx
 * <RichTextEditor
 *   name="content"
 *   control={control}
 *   label="Article Content"
 *   placeholder="Start writing your content..."
 *   maxHeight={400}
 * />
 * ```
 */
export function RichTextEditor<T extends FieldValues>({
  name,
  control,
  label,
  placeholder = 'Start typing...',
  helperText,
  maxHeight = 400,
  minHeight = 200,
  required = false,
  disabled = false,
  className,
}: RichTextEditorProps<T>): React.ReactElement {
  
  return (
    <Controller
      name={name}
      control={control}
      rules={{
        required: required ? `${label || name} is required` : false,
      }}
      render={({ field, fieldState: { error } }) => (
        <Box className={className}>
          {label && (
            <Typography
              variant="subtitle2"
              component="label"
              sx={{
                display: 'block',
                mb: 1,
                color: error ? colors.error : colors.textPrimary,
                fontWeight: 500,
              }}
            >
              {label}
              {required && (
                <Typography
                  component="span"
                  sx={{ color: colors.error, ml: 0.5 }}
                >
                  *
                </Typography>
              )}
            </Typography>
          )}
          
          <RichTextEditorContent
            value={field.value || ''}
            onChange={field.onChange}
            placeholder={placeholder}
            maxHeight={maxHeight}
            minHeight={minHeight}
            disabled={disabled}
            error={!!error}
          />
          
          {(error || helperText) && (
            <FormHelperText error={!!error} sx={{ mt: 0.5 }}>
              {error?.message || helperText}
            </FormHelperText>
          )}
        </Box>
      )}
    />
  );
}

// Internal component for editor content
interface RichTextEditorContentProps {
  value: string;
  onChange: (value: string) => void;
  placeholder: string;
  maxHeight: number | string;
  minHeight: number | string;
  disabled: boolean;
  error: boolean;
}

function RichTextEditorContent({
  value,
  onChange,
  placeholder,
  maxHeight,
  minHeight,
  disabled,
  error,
}: RichTextEditorContentProps): React.ReactElement {
  
  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        heading: {
          levels: [1, 2, 3],
        },
      }),
      Link.configure({
        openOnClick: false,
        linkOnPaste: true,
      }),
      Placeholder.configure({
        placeholder,
      }),
    ],
    content: value,
    editable: !disabled,
    onUpdate: useCallback(({ editor }) => {
      onChange(editor.getHTML());
    }, [onChange]),
  });

  // Sync external value changes
  React.useEffect(() => {
    if (editor && editor.getHTML() !== value) {
      editor.commands.setContent(value, false);
    }
  }, [editor, value]);

  // Handle link insertion
  const setLink = useCallback(() => {
    if (!editor) return;
    
    const previousUrl = editor.getAttributes('link').href;
    const url = window.prompt('Enter URL', previousUrl);
    
    if (url === null) return;
    
    if (url === '') {
      editor.chain().focus().extendMarkRange('link').unsetLink().run();
    } else {
      editor.chain().focus().extendMarkRange('link').setLink({ href: url }).run();
    }
  }, [editor]);

  if (!editor) {
    return (
      <Paper
        variant="outlined"
        sx={{
          p: 2,
          minHeight,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          borderColor: error ? colors.error : colors.border,
        }}
      >
        <Typography color={colors.textSecondary}>Loading editor...</Typography>
      </Paper>
    );
  }

  return (
    <Paper
      variant="outlined"
      sx={{
        borderColor: error ? colors.error : colors.border,
        borderWidth: error ? 2 : 1,
        '&:hover': {
          borderColor: error ? colors.error : colors.primary,
        },
        '&:focus-within': {
          borderColor: colors.primary,
          borderWidth: 2,
        },
        overflow: 'hidden',
        opacity: disabled ? 0.6 : 1,
      }}
    >
      {/* Toolbar */}
      <Box
        sx={{
          display: 'flex',
          flexWrap: 'wrap',
          gap: 0.5,
          p: 1,
          borderBottom: `1px solid ${colors.border}`,
          backgroundColor: colors.background,
        }}
      >
        <ToggleButtonGroup size="small" exclusive>
          <ToggleButton
            value="bold"
            selected={editor.isActive('bold')}
            onClick={() => editor.chain().focus().toggleBold().run()}
            disabled={!editor.can().chain().focus().toggleBold().run()}
            aria-label="Bold"
            title="Bold (Ctrl+B)"
          >
            <FormatBold fontSize="small" />
          </ToggleButton>
          <ToggleButton
            value="italic"
            selected={editor.isActive('italic')}
            onClick={() => editor.chain().focus().toggleItalic().run()}
            disabled={!editor.can().chain().focus().toggleItalic().run()}
            aria-label="Italic"
            title="Italic (Ctrl+I)"
          >
            <FormatItalic fontSize="small" />
          </ToggleButton>
        </ToggleButtonGroup>

        <Box sx={{ width: 1, height: 24, backgroundColor: colors.border, mx: 0.5 }} />

        <ToggleButtonGroup size="small" exclusive>
          <ToggleButton
            value="bulletList"
            selected={editor.isActive('bulletList')}
            onClick={() => editor.chain().focus().toggleBulletList().run()}
            disabled={!editor.can().chain().focus().toggleBulletList().run()}
            aria-label="Bullet List"
            title="Bullet List"
          >
            <FormatListBulleted fontSize="small" />
          </ToggleButton>
          <ToggleButton
            value="orderedList"
            selected={editor.isActive('orderedList')}
            onClick={() => editor.chain().focus().toggleOrderedList().run()}
            disabled={!editor.can().chain().focus().toggleOrderedList().run()}
            aria-label="Numbered List"
            title="Numbered List"
          >
            <FormatListNumbered fontSize="small" />
          </ToggleButton>
        </ToggleButtonGroup>

        <Box sx={{ width: 1, height: 24, backgroundColor: colors.border, mx: 0.5 }} />

        <ToggleButtonGroup size="small" exclusive>
          <ToggleButton
            value="link"
            selected={editor.isActive('link')}
            onClick={setLink}
            aria-label="Link"
            title="Add/Edit Link"
          >
            <LinkIcon fontSize="small" />
          </ToggleButton>
        </ToggleButtonGroup>

        <Box sx={{ width: 1, height: 24, backgroundColor: colors.border, mx: 0.5 }} />

        <ToggleButtonGroup size="small" exclusive>
          <ToggleButton
            value="heading"
            selected={editor.isActive('heading', { level: 2 })}
            onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
            disabled={!editor.can().chain().focus().toggleHeading({ level: 2 }).run()}
            aria-label="Heading"
            title="Toggle Heading"
          >
            <Title fontSize="small" />
          </ToggleButton>
        </ToggleButtonGroup>
      </Box>

      {/* Editor Content */}
      <Box
        sx={{
          minHeight,
          maxHeight,
          overflow: 'auto',
          p: 2,
          '& .ProseMirror': {
            outline: 'none',
            minHeight: `calc(${typeof minHeight === 'number' ? `${minHeight}px` : minHeight} - 32px)`,
            '& p': {
              margin: '0 0 0.75em 0',
              '&:last-child': {
                marginBottom: 0,
              },
            },
            '& h1, & h2, & h3': {
              margin: '0.5em 0 0.25em 0',
              fontWeight: 600,
            },
            '& h1': {
              fontSize: '1.75rem',
            },
            '& h2': {
              fontSize: '1.5rem',
            },
            '& h3': {
              fontSize: '1.25rem',
            },
            '& ul, & ol': {
              paddingLeft: '1.5em',
              margin: '0.5em 0',
            },
            '& li': {
              margin: '0.25em 0',
            },
            '& a': {
              color: colors.primary,
              textDecoration: 'underline',
              cursor: 'pointer',
            },
            '& blockquote': {
              borderLeft: `4px solid ${colors.primary}`,
              paddingLeft: '1em',
              margin: '0.5em 0',
              color: colors.textSecondary,
              fontStyle: 'italic',
            },
            '& code': {
              backgroundColor: colors.background,
              padding: '0.2em 0.4em',
              borderRadius: '3px',
              fontFamily: 'monospace',
              fontSize: '0.9em',
            },
            '& .is-editor-empty:first-child::before': {
              content: 'attr(data-placeholder)',
              float: 'left',
              color: colors.textSecondary,
              pointerEvents: 'none',
              height: 0,
            },
          },
        }}
      >
        <EditorContent editor={editor} />
      </Box>

      {/* Bubble Menu for quick formatting */}
      {editor && (
        <BubbleMenu
          editor={editor}
          tippyOptions={{ duration: 100 }}
          shouldShow={({ from, to }) => from !== to}
        >
          <Paper
            elevation={3}
            sx={{
              display: 'flex',
              gap: 0.5,
              p: 0.5,
            }}
          >
            <ToggleButton
              size="small"
              value="bold"
              selected={editor.isActive('bold')}
              onClick={() => editor.chain().focus().toggleBold().run()}
            >
              <FormatBold fontSize="small" />
            </ToggleButton>
            <ToggleButton
              size="small"
              value="italic"
              selected={editor.isActive('italic')}
              onClick={() => editor.chain().focus().toggleItalic().run()}
            >
              <FormatItalic fontSize="small" />
            </ToggleButton>
            <ToggleButton
              size="small"
              value="link"
              selected={editor.isActive('link')}
              onClick={setLink}
            >
              <LinkIcon fontSize="small" />
            </ToggleButton>
          </Paper>
        </BubbleMenu>
      )}
    </Paper>
  );
}

export default RichTextEditor;
