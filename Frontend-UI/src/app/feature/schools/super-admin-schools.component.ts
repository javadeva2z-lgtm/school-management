import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ManagedSchool, SchoolCreateRequest, SchoolManagementRequest } from '../../common/model/models';
import { SchoolService } from '../../core/auth/school.service';

@Component({
  selector: 'app-super-admin-schools',
  imports: [FormsModule, RouterLink],
  templateUrl: './super-admin-schools.component.html',
  styleUrl: './super-admin-schools.component.css'
})
export class SuperAdminSchoolsComponent {
  private readonly schoolService = inject(SchoolService);

  protected readonly schools = signal<ManagedSchool[]>([]);
  protected readonly editingSchoolId = signal<number | null>(null);
  protected readonly schoolName = signal('');
  protected readonly schoolCode = signal('');
  protected readonly token = signal('');
  protected readonly address = signal('');
  protected readonly phone = signal('');
  protected readonly email = signal('');
  protected readonly website = signal('');
  protected readonly principalName = signal('');
  protected readonly announcement = signal('');
  protected readonly isActive = signal(true);
  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly updatingSchoolStatusId = signal<number | null>(null);
  protected readonly errorMessage = signal('');
  protected readonly successMessage = signal('');

  constructor() {
    this.loadSchools();
  }

  protected startNewSchool(): void {
    this.editingSchoolId.set(null);
    this.schoolName.set('');
    this.schoolCode.set('');
    this.token.set('');
    this.address.set('');
    this.phone.set('');
    this.email.set('');
    this.website.set('');
    this.principalName.set('');
    this.announcement.set('');
    this.isActive.set(true);
    this.clearMessages();
  }

  protected editSchool(school: ManagedSchool): void {
    this.editingSchoolId.set(school.id);
    this.schoolName.set(school.schoolName);
    this.schoolCode.set(school.schoolCode);
    this.token.set(school.keywords ?? '');
    this.address.set(school.address ?? '');
    this.phone.set(school.phone ?? '');
    this.email.set(school.email ?? '');
    this.website.set(school.website ?? '');
    this.principalName.set(school.principalName ?? '');
    this.announcement.set(school.announcement ?? '');
    this.isActive.set(school.isActive);
    this.clearMessages();
  }

  protected submit(form: HTMLFormElement): void {
    if (!form.reportValidity()) {
      return;
    }

    const schoolName = this.schoolName().trim();
    const schoolCode = this.schoolCode().trim();
    if (!schoolName || !schoolCode || !this.token().trim() || this.isSaving()) {
      this.setError('School name, school code, and token are required.');
      return;
    }
    if (!this.editingSchoolId() && !/^[a-zA-Z0-9_]+$/.test(schoolCode)) {
      this.setError('School code may contain only letters, numbers, and underscores.');
      return;
    }

    const request: SchoolManagementRequest = {
      id: this.editingSchoolId() ?? undefined,
      schoolName,
      schoolCode,
      address: this.address().trim(),
      phone: this.phone().trim(),
      email: this.email().trim(),
      website: this.website().trim(),
      principalName: this.principalName().trim(),
      announcement: this.announcement().trim(),
      keywords: this.token().trim(),
      isActive: this.isActive()
    };

    this.isSaving.set(true);
    this.clearMessages();
    const operation = this.editingSchoolId()
      ? this.schoolService.updateSchool(request)
      : this.schoolService.createSchool({
        ...request,
        keywords: this.token().trim()
      } satisfies SchoolCreateRequest);
    operation.subscribe({
      next: () => {
        const message = this.editingSchoolId()
          ? `${schoolName} was updated.`
          : `${schoolName} was created.`;
        this.isSaving.set(false);
        this.loadSchools();
        this.startNewSchool();
        this.successMessage.set(message);
      },
      error: () => {
        this.setError(this.editingSchoolId()
          ? 'Unable to update this school. Check the details and try again.'
          : 'Unable to create this school. The school code may already be in use.');
        this.isSaving.set(false);
      }
    });
  }

  protected toggleSchoolActive(school: ManagedSchool): void {
    if (this.updatingSchoolStatusId() !== null || this.isSaving()) {
      return;
    }
    const nextActive = !school.isActive;
    const action = nextActive ? 'activate' : 'deactivate';
    if (!window.confirm(`${action[0].toUpperCase()}${action.slice(1)} "${school.schoolName}"?`)) {
      return;
    }

    this.updatingSchoolStatusId.set(school.id);
    this.clearMessages();
    this.schoolService.setSchoolActive(school.id, nextActive).subscribe({
      next: () => {
        this.schools.update(schools => schools.map(item =>
          item.id === school.id ? { ...item, isActive: nextActive } : item
        ));
        if (this.editingSchoolId() === school.id) {
          this.isActive.set(nextActive);
        }
        this.successMessage.set(`${school.schoolName} was ${nextActive ? 'activated' : 'deactivated'}.`);
        this.updatingSchoolStatusId.set(null);
      },
      error: () => {
        this.setError(`Unable to ${action} "${school.schoolName}". Please try again.`);
        this.updatingSchoolStatusId.set(null);
      }
    });
  }

  private loadSchools(): void {
    this.isLoading.set(true);
    this.schoolService.getSchoolsForManagement().subscribe({
      next: schools => {
        this.schools.set(schools);
        this.isLoading.set(false);
      },
      error: () => {
        this.setError('Unable to load schools. Please refresh and try again.');
        this.isLoading.set(false);
      }
    });
  }

  private setError(message: string): void {
    this.errorMessage.set(message);
    this.successMessage.set('');
  }

  private clearMessages(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
  }
}
