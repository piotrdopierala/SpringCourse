package pl.dopierala.SpringCourse.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.dopierala.SpringCourse.domain.Knight;
import pl.dopierala.SpringCourse.domain.PlayerInformation;
import pl.dopierala.SpringCourse.domain.Quest;
import pl.dopierala.SpringCourse.domain.repository.KnightRepository;
import pl.dopierala.SpringCourse.domain.repository.PlayerInformationRepository;
import pl.dopierala.SpringCourse.domain.repository.QuestRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;


@Component
public class KnightService {

    @Autowired
    KnightRepository repository;

    @Autowired
    QuestRepository questRepository;

    @Autowired
    PlayerInformationRepository playerInformationRepository;


    public List<Knight> getAllKnights() {
        List<Knight> allKnights = new ArrayList<>(repository.getAllKnights());
        return allKnights;
    }

    public void saveKnight(Knight newKnight) {
        repository.createKnight(newKnight);
    }

    public Knight getKnight(Integer id) {
        return repository.getKnightById(id);
    }

    public void deleteKnight(Integer id) {
        repository.deleteKnight(id);
    }

    public void updateKnight(Knight knight) {
        repository.updateKnight(knight.getId(), knight);
    }

    public int collectRewards() {
        Predicate<Knight> completedNotNullQuestsPredicate = k -> {
            if (k.getQuest() != null) {
                return k.getQuest().isCompleted();
            } else
                return false;
        };

        int sum = repository.getAllKnights().stream()
                .filter(completedNotNullQuestsPredicate)
                .mapToInt(knight -> knight.getQuest().getReward())
                .sum();

        repository.getAllKnights().stream()
                .filter(completedNotNullQuestsPredicate)
                .forEach(Knight::deleteQuest);

        return sum;
    }

    @Transactional
    public void getMyGold() {

        List<Knight> allKnights = getAllKnights();
        allKnights.forEach((k) -> {
                    if (k.getQuest() != null) {
                        boolean completed = k.getQuest().isCompleted();
                        if(completed){
                            questRepository.update(k.getQuest());
                        }
                    }
                }
        );

        PlayerInformation first = playerInformationRepository.getFirst();
        int currentGold = first.getGold();
        first.setGold(currentGold + collectRewards());
    }
}
