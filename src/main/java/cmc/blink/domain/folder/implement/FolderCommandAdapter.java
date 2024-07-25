package cmc.blink.domain.folder.implement;

import cmc.blink.domain.folder.persistence.Folder;
import cmc.blink.domain.folder.persistence.FolderRepository;
import cmc.blink.global.annotation.Adapter;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class FolderCommandAdapter {

    private final FolderRepository folderRepository;

    public Folder create(Folder folder) {
        return folderRepository.save(folder);
    }


}
