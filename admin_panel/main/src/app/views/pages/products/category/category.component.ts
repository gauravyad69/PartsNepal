// category.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CategoryService } from '../services/category.service';
import { HotToastService } from '@ngneat/hot-toast';
import { CategoryModelReq, CategoryModelRes } from '../../models/product.model';

@Component({
  selector: 'app-category',
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule]
})
export class CategoryComponent implements OnInit {
  categories: CategoryModelRes[] = [];
  categoryForm!: FormGroup;
  isEditing = false;
  editingId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private toast: HotToastService
  ) {
    this.initForm();
  }

  ngOnInit() {
    this.loadCategories();
  }

  private initForm() {
    this.categoryForm = this.fb.group({
      categoryName: ['', Validators.required],
      subCategoryName: ['', Validators.required]
    });
  }

  loadCategories() {
    this.categoryService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error) => {
        this.toast.error('Failed to load categories');
      }
    });
  }

  onSubmit() {
    if (this.categoryForm.invalid) {
      this.toast.error('Please fill all required fields');
      return;
    }

    const categoryData: CategoryModelReq = {
      ...this.categoryForm.value,
      createdAt: this.isEditing ? null : Date.now(),
      updatedAt: Date.now(),
      version: this.isEditing ? null : 1
    };

    if (this.isEditing && this.editingId) {
      this.categoryService.updateCategory(this.editingId, categoryData).subscribe({
        next: () => {
          this.toast.success('Category updated successfully');
          this.resetForm();
          this.loadCategories();
        },
        error: (error) => {
          this.toast.error('Failed to update category');
        }
      });
    } else {
      this.categoryService.createCategory(categoryData).subscribe({
        next: () => {
          this.toast.success('Category created successfully');
          this.resetForm();
          this.loadCategories();
        },
        error: (error) => {
          this.toast.error('Failed to create category');
        }
      });
    }
  }

  editCategory(category: CategoryModelRes) {
    this.isEditing = true;
    this.editingId = category.categoryId;
    this.categoryForm.patchValue({
      categoryName: category.categoryName,
      subCategoryName: category.subCategoryName
    });
  }

  deleteCategory(id: string) {
    if (confirm('Are you sure you want to delete this category?')) {
      this.categoryService.deleteCategory(id).subscribe({
        next: () => {
          this.toast.success('Category deleted successfully');
          this.loadCategories();
        },
        error: (error) => {
          this.toast.error('Failed to delete category');
        }
      });
    }
  }

  resetForm() {
    this.isEditing = false;
    this.editingId = null;
    this.categoryForm.reset();
  }
}