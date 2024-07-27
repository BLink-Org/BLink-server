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


}
