import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { PeopleService } from './people.service';
import { Student, ClassSectionOption } from '../../common/model/models';
import { ClassSectionService } from '../class-section/class-section.service';

interface StudentFormModel {
  name: string;
  gender: string;
  rollNumber: string;
  dob: string;
  admissionDate: string;
  address: string;
  fatherName: string;
  motherName: string;
  parentMobile: string;
  className: string;
  section: string;
  email: string;
}

@Component({
  selector: 'app-admin-students',
  imports: [RouterLink, FormsModule],
  templateUrl: './admin-students.component.html',
  styleUrl: './admin-people-management.css'
})
export class AdminStudentsComponent {
  private readonly peopleService = inject(PeopleService);
  private readonly classSectionService = inject(ClassSectionService);
  protected readonly fields: Array<keyof StudentFormModel> = ['name', 'gender', 'rollNumber', 'dob', 'admissionDate', 'address', 'fatherName', 'motherName', 'parentMobile', 'className', 'section', 'email'];
  protected readonly classOptions = signal<ClassSectionOption[]>([]);
  protected readonly sectionOptions = computed(() => this.classOptions().find(option => Number(option.classId) === this.selectedClassId())?.sections ?? []);
  protected readonly studentModel: StudentFormModel = {
    name: '',
    gender: '',
    rollNumber: '',
    dob: '',
    admissionDate: '',
    address: '',
    fatherName: '',
    motherName: '',
    parentMobile: '',
    className: '',
    section: '',
    email: ''
  };
  protected activeTab: 'add' | 'manage' = 'add';
  protected readonly selectedClassId = signal(2);
  protected readonly selectedSection = signal('B');
  protected readonly students = signal<Student[]>([]);
  protected readonly loadingStudents = signal(false);
  protected editingAdmissionNumber: number | null = null;
  protected message = '';
  protected error = '';

  constructor() {
    this.classSectionService.getAll().subscribe(options => {
      this.classOptions.set(options);
      const firstClass = options[0];
      this.selectedClassId.set(Number(firstClass?.classId ?? 0));
      this.selectedSection.set(firstClass?.sections[0]?.sectionName ?? '');
    });
  }

  protected labelFor(field: string): string {
    return { rollNumber: 'Roll no', dob: 'Date of birth', className: 'Class', parentMobile: 'Parent mobile' }[field] ?? field;
  }

  protected save(): void {
    if (Object.values(this.studentModel).some(value => !String(value).trim())) {
      this.error = 'Complete all student fields before saving.';
      this.message = '';
      return;
    }
    const student: Omit<Student, 'admissionNumber'> = {
      name: this.studentModel['name'],
      gender: this.studentModel['gender'],
      email: this.studentModel['email'],
      rollNumber: Number(this.studentModel['rollNumber']),
      classId: Number(this.studentModel['className']),
      sectionName: this.studentModel['section'],
      fatherName: this.studentModel['fatherName'],
      motherName: this.studentModel['motherName'],
      dateOfBirth: this.studentModel['dob'],
      admissionDate: this.studentModel['admissionDate'],
      address: this.studentModel['address'],
      parentPhone: this.studentModel['parentMobile']
    };
    const editingAdmissionNumber = this.editingAdmissionNumber;
    const request = editingAdmissionNumber === null
      ? this.peopleService.createStudent(student)
      : this.peopleService.updateStudent({ ...student, admissionNumber: editingAdmissionNumber });
    request.subscribe({
      next: createdStudent => {
        this.message = editingAdmissionNumber === null
          ? `Student added successfully. Admission number: ${createdStudent.admissionNumber}.`
          : 'Student details updated successfully.';
        this.error = '';
        this.editingAdmissionNumber = null;
        this.fields.forEach(field => {
          this.studentModel[field] = '';
        });
        if (this.activeTab === 'manage') this.loadStudents();
      },
      error: () => {
        this.error = editingAdmissionNumber === null
          ? 'Unable to add student. Please try again.'
          : 'Unable to update student details.';
        this.message = '';
      }
    });
  }

  protected showManage(): void {
    this.activeTab = 'manage';
    this.loadStudents();
  }

  protected onManageClassChange(event: Event): void {
    const classId = Number((event.target as HTMLSelectElement).value);
    this.selectedClassId.set(classId);
    this.selectedSection.set(this.classOptions().find(option => Number(option.classId) === classId)?.sections[0]?.sectionName ?? '');
    this.loadStudents();
  }

  protected onManageSectionChange(event: Event): void {
    this.selectedSection.set((event.target as HTMLSelectElement).value);
    this.loadStudents();
  }

  protected editStudent(student: Student): void {
    this.editingAdmissionNumber = student.admissionNumber;
    this.fields.forEach(field => {
      this.studentModel[field] = '';
    });
    Object.assign(this.studentModel, {
      name: student.name,
      gender: student.gender,
      rollNumber: String(student.rollNumber),
      dob: student.dateOfBirth,
      admissionDate: student.admissionDate ?? '',
      address: student.address,
      fatherName: student.fatherName,
      motherName: student.motherName,
      parentMobile: student.parentPhone,
      className: String(student.classId),
      section: student.sectionName,
      email: student.email
    });
    this.activeTab = 'add';
    this.message = '';
    this.error = '';
  }

  private loadStudents(): void {
    this.loadingStudents.set(true);
    this.error = '';
    this.peopleService.getStudentsByClassAndSection(this.selectedClassId(), this.selectedSection()).subscribe({
      next: students => {
        this.students.set(students);
        this.loadingStudents.set(false);
      },
      error: () => {
        this.students.set([]);
        this.loadingStudents.set(false);
        this.error = 'Unable to load students for the selected class and section.';
      }
    });
  }
}
