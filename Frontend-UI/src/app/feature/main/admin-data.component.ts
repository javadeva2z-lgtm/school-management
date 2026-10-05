import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ClassSectionOption } from '../../common/model/models';
import { ClassSectionService } from '../class-section/class-section.service';
import { AdminDataService, AdminDataType } from './admin-data.service';

@Component({
  selector: 'app-admin-data',
  imports: [RouterLink],
  templateUrl: './admin-data.component.html',
  styleUrl: './admin-data.component.css'
})
export class AdminDataComponent {
  private readonly adminDataService = inject(AdminDataService);
  private readonly classSectionService = inject(ClassSectionService);

  protected readonly adminDataTypes: AdminDataType[] = ['Student', 'Teacher', 'Section'];
  protected classOptions: ClassSectionOption[] = [];
  protected activeDataTab: 'import' | 'sample' | 'records' = 'import';
  protected selectedImportType: AdminDataType = 'Student';
  protected selectedSampleType: AdminDataType = 'Student';
  protected selectedExportType: AdminDataType = 'Student';
  protected selectedExportClassId = '0';
  protected selectedExportSectionName = '0';
  protected selectedFile: File | null = null;
  protected dataMessage = '';
  protected dataError = '';
  private classOptionsLoaded = false;

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
    this.dataMessage = '';
    this.dataError = '';
  }

  protected selectDataTab(tab: 'import' | 'sample' | 'records'): void {
    this.activeDataTab = tab;
    this.dataMessage = '';
    this.dataError = '';
    if (tab === 'records') {
      this.loadClassOptions();
    }
  }

  protected onExportTypeChange(value: string): void {
    this.selectedExportType = value as AdminDataType;
    this.selectedExportClassId = '0';
    this.selectedExportSectionName = '0';
    if (this.selectedExportType === 'Student') {
      this.loadClassOptions();
    }
  }

  protected onExportClassChange(value: string): void {
    this.selectedExportClassId = value;
    this.selectedExportSectionName = '0';
  }

  protected get selectedExportSections(): ClassSectionOption['sections'] {
    return this.classOptions.find(option => option.classId === this.selectedExportClassId)?.sections ?? [];
  }

  protected importSelectedFile(): void {
    if (!this.selectedFile) {
      this.dataError = 'Choose a CSV file before importing.';
      this.dataMessage = '';
      return;
    }

    this.dataMessage = '';
    this.dataError = '';
    this.adminDataService.importFile(this.selectedFile, this.selectedImportType).subscribe({
      next: () => {
        this.dataMessage = `${this.selectedImportType} records imported successfully.`;
        this.selectedFile = null;
      },
      error: () => {
        this.dataError = 'The file could not be imported. Check the CSV format and try again.';
      }
    });
  }

  protected downloadSample(): void {
    this.adminDataService.exportSample(this.selectedSampleType).subscribe({
      next: file => this.downloadBlob(`${this.selectedSampleType.toLowerCase().replaceAll(' & ', '-')}-sample.csv`, file),
      error: () => {
        this.dataError = 'The sample template could not be downloaded. Please try again.';
      }
    });
  }

  protected exportRecords(): void {
    this.dataMessage = '';
    this.dataError = '';
    const classId = this.selectedExportType === 'Student' ? this.selectedExportClassId : '0';
    const sectionName = this.selectedExportType === 'Student' ? this.selectedExportSectionName : '0';
    this.adminDataService.exportRecords(this.selectedExportType, classId, sectionName).subscribe({
      next: file => this.downloadBlob(`${this.selectedExportType.toLowerCase().replaceAll(' & ', '-')}-records.csv`, file),
      error: () => {
        this.dataError = 'Existing records could not be exported. Please try again.';
      }
    });
  }

  private loadClassOptions(): void {
    if (this.classOptionsLoaded) {
      return;
    }
    this.classSectionService.getAll().subscribe({
      next: options => {
        this.classOptions = options;
        this.classOptionsLoaded = true;
      },
      error: () => {
        this.dataError = 'Class and section options could not be loaded. Please try again.';
      }
    });
  }

  private downloadCsv(fileName: string, content: string): void {
    this.downloadBlob(fileName, new Blob([content], { type: 'text/csv;charset=utf-8' }));
  }

  private downloadBlob(fileName: string, blob: Blob): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    URL.revokeObjectURL(url);
  }
}
