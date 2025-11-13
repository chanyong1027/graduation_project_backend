/**
 * ReviewPage 컴포넌트
 *
 * @description
 * - 내 리뷰 페이지
 * - 읽고 있는 책 / 다 읽은 책 탭 구분
 * - 책 정보 + 리뷰 작성 공간
 * - 페이지네이션 (10개씩)
 */

import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBookStateStore } from '@/store/useBookStateStore';
import { findBookById } from '@/utils/mockData';
import type { UserBookState } from '@/types/user';

export const ReviewPage = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'READING' | 'READ'>('READ');
  const [currentPage, setCurrentPage] = useState(1);
  const ITEMS_PER_PAGE = 10;

  // userBookStates 전체 구독
  const allBookStates = useBookStateStore((state) => state.userBookStates);
  const { setBookState } = useBookStateStore();

  // 상태별 필터링
  const readingBooks = useMemo(
    () => allBookStates.filter((item) => item.state === 'READING'),
    [allBookStates]
  );

  const readBooks = useMemo(
    () => allBookStates.filter((item) => item.state === 'READ'),
    [allBookStates]
  );

  // 현재 탭의 책 목록
  const currentBooks = activeTab === 'READING' ? readingBooks : readBooks;

  // 페이지네이션
  const totalPages = Math.ceil(currentBooks.length / ITEMS_PER_PAGE);
  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
  const endIndex = startIndex + ITEMS_PER_PAGE;
  const paginatedBooks = currentBooks.slice(startIndex, endIndex);

  // 탭 변경 시 페이지 초기화
  const handleTabChange = (tab: 'READING' | 'READ') => {
    setActiveTab(tab);
    setCurrentPage(1);
  };

  // 리뷰 저장
  const handleSaveReview = (bookState: UserBookState, newComment: string) => {
    setBookState({
      ...bookState,
      comment: newComment,
      updatedAt: new Date().toISOString(),
    });
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-5xl mx-auto px-4">
        {/* 헤더 */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-900 mb-2">내 리뷰</h1>
          <p className="text-sm text-gray-600">
            읽은 책과 읽고 있는 책에 대한 리뷰를 작성해보세요
          </p>
        </div>

        {/* 탭 */}
        <div className="flex gap-2 mb-6">
          <button
            onClick={() => handleTabChange('READ')}
            className={`
              px-6 py-3 rounded-xl text-sm font-medium transition-colors
              ${
                activeTab === 'READ'
                  ? 'bg-primary text-white'
                  : 'bg-white text-gray-600 hover:bg-gray-50'
              }
            `}
          >
            다 읽은 책 ({readBooks.length})
          </button>
          <button
            onClick={() => handleTabChange('READING')}
            className={`
              px-6 py-3 rounded-xl text-sm font-medium transition-colors
              ${
                activeTab === 'READING'
                  ? 'bg-primary text-white'
                  : 'bg-white text-gray-600 hover:bg-gray-50'
              }
            `}
          >
            읽고 있는 책 ({readingBooks.length})
          </button>
        </div>

        {/* 리뷰 목록 */}
        {paginatedBooks.length === 0 ? (
          <div className="bg-white rounded-2xl p-12 text-center">
            <div className="text-gray-400 text-6xl mb-4">📚</div>
            <h3 className="text-lg font-semibold text-gray-700 mb-2">
              {activeTab === 'READ' ? '완독한 책이 없습니다' : '읽고 있는 책이 없습니다'}
            </h3>
            <p className="text-sm text-gray-500 mb-6">
              책을 추가하고 리뷰를 작성해보세요!
            </p>
            <button
              onClick={() => navigate('/')}
              className="px-6 py-2 bg-primary text-white rounded-xl hover:opacity-90 transition-all"
            >
              책 검색하기
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {paginatedBooks.map((bookState) => {
              const book = findBookById(bookState.bookId);
              return (
                <ReviewCard
                  key={bookState.bookId}
                  bookState={bookState}
                  book={book}
                  onSave={handleSaveReview}
                  onViewDetail={() => navigate(`/book/${bookState.bookId}`)}
                />
              );
            })}
          </div>
        )}

        {/* 페이지네이션 */}
        {totalPages > 1 && (
          <div className="mt-8 flex justify-center gap-2">
            <button
              onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
              disabled={currentPage === 1}
              className="
                px-4 py-2 rounded-xl text-sm font-medium
                bg-white text-gray-700 border border-gray-300
                hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed
                transition-colors
              "
            >
              이전
            </button>

            {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
              <button
                key={page}
                onClick={() => setCurrentPage(page)}
                className={`
                  px-4 py-2 rounded-xl text-sm font-medium transition-colors
                  ${
                    currentPage === page
                      ? 'bg-primary text-white'
                      : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
                  }
                `}
              >
                {page}
              </button>
            ))}

            <button
              onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))}
              disabled={currentPage === totalPages}
              className="
                px-4 py-2 rounded-xl text-sm font-medium
                bg-white text-gray-700 border border-gray-300
                hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed
                transition-colors
              "
            >
              다음
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

/**
 * ReviewCard 컴포넌트
 */
interface ReviewCardProps {
  bookState: UserBookState;
  book: any;
  onSave: (bookState: UserBookState, comment: string) => void;
  onViewDetail: () => void;
}

const ReviewCard: React.FC<ReviewCardProps> = ({ bookState, book, onSave, onViewDetail }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [comment, setComment] = useState(bookState.comment || '');

  const handleSave = () => {
    onSave(bookState, comment);
    setIsEditing(false);
  };

  const handleCancel = () => {
    setComment(bookState.comment || '');
    setIsEditing(false);
  };

  return (
    <div className="bg-white rounded-2xl p-6 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex gap-6">
        {/* 책 정보 (왼쪽) */}
        <div className="flex-shrink-0 w-32">
          <div className="aspect-[2/3] rounded-lg overflow-hidden mb-3 cursor-pointer" onClick={onViewDetail}>
            {book?.coverUrl ? (
              <img
                src={book.coverUrl}
                alt={book.title}
                className="w-full h-full object-cover hover:scale-105 transition-transform"
              />
            ) : (
              <div className="w-full h-full bg-gray-200 flex items-center justify-center">
                <span className="text-gray-400 text-xs">No Image</span>
              </div>
            )}
          </div>
          <h3
            className="text-sm font-semibold text-gray-900 line-clamp-2 mb-1 cursor-pointer hover:text-primary"
            onClick={onViewDetail}
          >
            {book?.title || `도서 #${bookState.bookId}`}
          </h3>
          <p className="text-xs text-gray-500">{book?.author || '저자 미상'}</p>

          {/* 별점 */}
          {bookState.rating && (
            <div className="flex items-center gap-1 mt-2">
              <span className="text-yellow-400 text-sm">{'★'.repeat(bookState.rating)}</span>
              <span className="text-xs text-gray-500">{bookState.rating}.0</span>
            </div>
          )}

          {/* 읽은 날짜 */}
          {bookState.endDate && (
            <p className="text-xs text-gray-400 mt-2">
              완독일: {new Date(bookState.endDate).toLocaleDateString('ko-KR')}
            </p>
          )}
        </div>

        {/* 리뷰 영역 (오른쪽) */}
        <div className="flex-1">
          <div className="flex items-center justify-between mb-3">
            <h4 className="text-base font-semibold text-gray-900">나의 리뷰</h4>
            {!isEditing && (
              <button
                onClick={() => setIsEditing(true)}
                className="text-sm text-primary hover:text-blue-600 font-medium"
              >
                {bookState.comment ? '수정' : '작성'}
              </button>
            )}
          </div>

          {isEditing ? (
            <div>
              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="이 책에 대한 생각을 자유롭게 작성해보세요..."
                className="
                  w-full h-40 px-4 py-3 border border-gray-300 rounded-xl
                  focus:ring-2 focus:ring-blue-400 focus:border-transparent
                  resize-none text-sm
                "
                maxLength={1000}
              />
              <div className="flex items-center justify-between mt-2">
                <span className="text-xs text-gray-500">{comment.length}/1000자</span>
                <div className="flex gap-2">
                  <button
                    onClick={handleCancel}
                    className="px-4 py-2 rounded-xl text-sm font-medium bg-gray-100 text-gray-700 hover:bg-gray-200 transition-colors"
                  >
                    취소
                  </button>
                  <button
                    onClick={handleSave}
                    className="px-4 py-2 rounded-xl text-sm font-medium bg-primary text-white hover:opacity-90 transition-all"
                  >
                    저장
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div>
              {bookState.comment ? (
                <p className="text-sm text-gray-700 whitespace-pre-wrap leading-relaxed">
                  {bookState.comment}
                </p>
              ) : (
                <p className="text-sm text-gray-400 italic">
                  아직 리뷰를 작성하지 않았습니다. 이 책에 대한 생각을 기록해보세요!
                </p>
              )}
              {bookState.updatedAt && bookState.comment && (
                <p className="text-xs text-gray-400 mt-3">
                  마지막 수정: {new Date(bookState.updatedAt).toLocaleString('ko-KR')}
                </p>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ReviewPage;
