// category.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CarrouselService } from '../services/carrousel.service';
import { HotToastService } from '@ngneat/hot-toast';
import { CarrouselReq } from '../../models/product.model';

@Component({
  selector: 'app-carrousel',
  templateUrl: './carrousel.component.html',
  styleUrls: ['./carrousel.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule]
})
export class CarrouselComponent implements OnInit {
  carrousel: CarrouselReq[] = [];
  carrouselForm!: FormGroup;
  isEditing = false;
  editingId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private carrouselService: CarrouselService,
    private toast: HotToastService
  ) {
    this.initForm();
  }

  ngOnInit() {
    this.loadCarrousel();
  }

  private initForm() {
    this.carrouselForm = this.fb.group({
      carrouselId: ['', Validators.required],
      imageUrl: ['', Validators.required]
    });
  }

  loadCarrousel() {
    this.carrouselService.getCarrousel().subscribe({
      next: (carrousel) => {
        this.carrousel = carrousel;
      },
      error: (error) => {
        this.toast.error('Failed to load carrousel');
      }
    });
  }

  onSubmit() {
    if (this.carrouselForm.invalid) {
      this.toast.error('Please fill all required fields');
      return;
    }

    const carrouselData: CarrouselReq = {
      ...this.carrouselForm.value,
      createdAt: this.isEditing ? null : Date.now(),
      updatedAt: Date.now(),
      version: this.isEditing ? null : 1
    };

    if (this.isEditing && this.editingId) {
      this.carrouselService.updateCarrousel(this.editingId, carrouselData).subscribe({
        next: () => {
          this.toast.success('Carrousel updated successfully');
          this.resetForm();
          this.loadCarrousel();
        },
        error: (error) => {
          this.toast.error('Failed to update carrousel');
        }
      });
    } else {
      this.carrouselService.createCarrousel(carrouselData).subscribe({
        next: () => {
          this.toast.success('Carrousel created successfully');
          this.resetForm();
          this.loadCarrousel();
        },
        error: (error) => {
          this.toast.error('Failed to create carrousel');
        }
      });
    }
  }

  editCarrousel(carrousel: CarrouselReq) {
    this.isEditing = true;
    this.editingId = carrousel.carrouselId;
    this.carrouselForm.patchValue({
      carrouselId: carrousel.carrouselId,
      imageUrl: carrousel.imageUrl
    });
  }

  deleteCarrousel(id: string) {
    if (confirm('Are you sure you want to delete this carrousel?')) {
      this.carrouselService.deleteCarrousel(id).subscribe({
        next: () => {
          this.toast.success('Carrousel deleted successfully');
          this.loadCarrousel();
        },
        error: (error: any) => {
          this.toast.error('Failed to delete carrousel');
        }
      });
    }
  }

  resetForm() {
    this.isEditing = false;
    this.editingId = null;
    this.carrouselForm.reset();
  }
}