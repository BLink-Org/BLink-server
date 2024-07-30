package cmc.blink.domain.folder.implement;

import cmc.blink.domain.folder.persistence.Folder;
import cmc.blink.domain.folder.persistence.FolderRepository;
import cmc.blink.domain.folder.presentation.dto.FolderRequest;
import cmc.blink.global.annotation.Adapter;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class FolderCommandAdapter {

    private final FolderRepository folderRepository;

    public Folder create(Folder folder) {
        return folderRepository.save(folder);
    }

    public void updateTitle(Folder folder, FolderRequest.FolderTitleUpdateDto updateDto) {
        folder.updateTitle(updateDto.getTitle());
        folderRepository.save(folder);
    }

    public Folder updateLastLinkedAt(Folder folder) {
        folder.updatelastLinkedAt();
        return folderRepository.save(folder);
    }

    public void swapSortOrder(Folder folder1, Folder folder2) {
        int sortOrder1 = folder1.getSortOrder();
        int sortOrder2 = folder2.getSortOrder();

        folder1.updateSortOrder(sortOrder2);
        folder2.updateSortOrder(sortOrder1);

        folderRepository.save(folder1);
        folderRepository.save(folder2);
    }

    public void delete(Folder folder) {
        folderRepository.delete(folder);
    }

}
