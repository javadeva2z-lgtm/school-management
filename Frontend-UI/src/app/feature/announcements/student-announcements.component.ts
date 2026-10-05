import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AnnouncementsService } from './announcements.service';
import { Announcement, classDisplayName, ClassSectionOption } from '../../common/model/models';
import { ClassSectionService } from '../class-section/class-section.service';

@Component({
  selector: 'app-student-announcements',
  imports: [RouterLink],
  templateUrl: './student-announcements.component.html'
})
export class StudentAnnouncementsComponent {
  private readonly announcementsService = inject(AnnouncementsService);
  private readonly classSectionService = inject(ClassSectionService);
  protected readonly announcements = signal<Announcement[]>([]);
  protected readonly selectedAnnouncement = signal<Announcement | null>(null);
  protected readonly classOptions = signal<ClassSectionOption[]>([]);
  protected readonly classDisplayName = (classId: number | string) => classDisplayName(classId, this.classOptions());

  constructor() {
    this.classSectionService.getAll().subscribe(options => this.classOptions.set(options));
    this.loadAnnouncements();
  }

  protected loadAnnouncements(): void {
    this.announcementsService.getAll('', '').subscribe(data => {
      this.announcements.set(data);
      this.selectedAnnouncement.set(data[0] ?? null);
    });
  }

  protected selectAnnouncement(announcement: Announcement): void {
    this.selectedAnnouncement.set(announcement);
  }
}
