package notfound.ballog.domain.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import notfound.ballog.common.response.BaseResponseStatus;
import notfound.ballog.domain.user.dto.PlayerCardStatListDto;
import notfound.ballog.domain.user.entity.PlayerCard;
import notfound.ballog.domain.user.entity.User;
import notfound.ballog.domain.user.repository.PlayerCardRepository;
import notfound.ballog.domain.user.repository.UserRepository;
import notfound.ballog.domain.user.response.GetPlayerCardResponse;
import notfound.ballog.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerCardService {

    private final PlayerCardRepository playerCardRepository;

    private final UserRepository userRepository;

    @Transactional
    public void addPlayerCard(User savedUser) {
        PlayerCard newPlayerCard = PlayerCard.addBaseCard(savedUser);

        playerCardRepository.save(newPlayerCard);
    }

    @Transactional
    public GetPlayerCardResponse getPlayerCard(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.USER_NOT_FOUND));

        PlayerCard playerCard = playerCardRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new NotFoundException(BaseResponseStatus.PLAYER_CARD_NOT_FOUND));

        PlayerCardStatListDto playerCardStatList = PlayerCardStatListDto.of(playerCard);

        return GetPlayerCardResponse.of(user.getProfileImageUrl(), playerCard, playerCardStatList);
    }
}