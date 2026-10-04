import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { PeopleService } from './people.service';
import { classDisplayName, ClassSectionOption, Student } from '../../common/model/models';
import { FormsModule } from '@angular/forms';
import { ProfileService } from '../profile/profile.service';
import { ClassSectionService } from '../class-section/class-section.service';

@Component({
  selector: 'app-students',
  imports: [FormsModule],
  templateUrl: './students.component.html'
})
export class StudentsComponent {
  private readonly profileService = inject(ProfileService);
  private readonly peopleService = inject(PeopleService);
  private readonly classSectionService = inject(ClassSectionService);

  protected readonly students = signal<Student[]>([]);
  protected readonly classOptions = signal<ClassSectionOption[]>([]);
  protected readonly classDisplayName = (classId: number | string) => classDisplayName(classId, this.classOptions());

  constructor() {
    this.classSectionService.getAll().subscribe(options => this.classOptions.set(options));
    this.loadLoginUserProfile();
  }

  protected loadLoginUserProfile(): void {
    this.profileService.getProfile().subscribe(profile => {
      const classId = profile.className;
      const sectionName = profile.sectionName ?? '';
      this.loadStudents(Number(classId), sectionName);
    });
  }

  private loadStudents(classId: number, sectionName: string): void {
    this.peopleService.getStudentsByClassAndSection(classId, sectionName)
      .subscribe(students => {
        this.students.set(students);
      });
  }
}