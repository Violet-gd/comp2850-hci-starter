# axe DevTools Audit Report — Week 7

**Date**: [2025-11-16]
**URL**: http://localhost:8080/tasks
**Tool**: axe DevTools 4.x
**Scope**: Full page scan (add form + task list)

---

## Summary
- **Critical**: 0
- **Serious**: 6
- **Moderate**: 0
- **Minor**: 0
- **Total**: 6 issues

---

## Critical Issues
None detected.

---

## Serious Issues

### Issue 1-5: Insufficient color contrast (Serious)
**Element**: `<button type="submit">Add Task</button> , <button type="submit" aria-label="Edit task: Save">Edit</button>
, <button type="submit" aria-label="Delete task: Save">Delete</button>, <button type="submit" aria-label="Edit task: drinking">Edit</button>,<button type="submit" aria-label="Delete task: drinking">Delete</button>`

**Rule**: `color-contrast` (WCAG 1.4.3)
**Description**:  Text color #6c757d on white background = 4.2:1 (fails AA 4.5:1)
**Impact**: People with low vision struggle to read button text.
**Fix**: Change button color to #ffffff (white gray, over 7:1 contrast).
**Status**:✅ **CONFIRMED** — Add to backlog as High severity.

### Issue 6: Link Must Have Discernible Text (Serious)
**Element**: `<a href="/about"></a>`
**Rule**: `link-name` (WCAG 2.4.4)
**Description**: Link contains no visible text. No aria-label, aria-labelledby, or title. Screen readers cannot identify the link purpose
**Impact**: Screen-reader users cannot understand what this link leads to.
**Fix**:<a href="/about">About</a> or <a href="/about" aria-label="About page"></a>
**Status**: ✅ **CONFIRMED** — Add to backlog as medium severity.

---


---

## Actions
1. **False positive (Issue 1)**: Verify label exists with manual inspection
2. **High priority (Issue 2)**: Fix contrast ratio → Add to backlog
3. **Verified (Issue 3)**: No action needed

---

**Next step**: Manual testing to catch issues axe misses (focus order, SR announcements, keyboard traps).
